import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;

public class Car {

	// Retangulo que representa o carro
	private static Path2D carRect;
	// Direcao do movimento do carro. É um valor em angulos radianos, entre 0 e 2PI.
	// Para incrementar ou decrementar essa variável, é preciso usar a funçao getDirectionVal,
	// que implementa um incremento circular entre 0 e 2PI.
	// Um valor não vindo da função getDirectionVal aqui causará bugs no programa.
	private static double direction;
	// Velocidade do movimento do carro. O carro anda speed pixels por ciclo. Valor padrão é 1
	private static int speed;

	// Linha que representa o sensor infravermelho esquerdo
	private static Path2D leftInfraredLine;
	// Alcance do sensor esquerdo
	private static double leftInfraredDist;
	// Linha que representa o sensor infravermelho direito
	private static Path2D rightInfraredLine;
	// Alcance do sensor direito
	private static double rightInfraredDist;

	// Linha que representa o sensor ultrassonico
	private static Path2D ultrasonicLine;
	// Direção da linha do sensor ultrassonico nesse instante. A variável é um offset
	// em relação a direção do carro. A direção então é DIRECAO_CARRO + ultrasonicDirection.
	// Valor em angulos radianos
	private static double ultrasonicDirection;
	// Alcance do sensor ultrassonico
	private static double ultrasonicDist;
	// Valor de entrada do sensor no limite esquerdo do arco
	private static double ultrasonicDistLeft;
	// Valor de entrada do sensor no limite direito do arco
	private static double ultrasonicDistRight;
	
	private static int collisionCnt;
	
	public static int getStats() {
		return collisionCnt;
	}
	
	// Parametros de movimento para um ciclo. Esse objeto é inserido na fila paramsQueue.
	// Isso é usado para mandar o carro fazer um movimento longo, que precisará de vários ciclos
	// para ser concluido. Por exemplo a movimentação do sensor ultrassonico, ou o giro do carro parado.
	// Enfileira-se o passo a passo do movimento, com um conjunto de objetos desse tipo. 
	// O ciclo do carro tem uma execução diferente, dependendo se a fila paramsQueue está vazia ou não.
	static class CycleParams {
		public double rotation;
		public double ultrasonicRotation;
		public int speed;
		
		public CycleParams(double rotation, double ultrasonicRotation, int speed) {
			this.rotation = rotation;
			this.ultrasonicRotation = ultrasonicRotation;
			this.speed = speed;
		}
	}
	
	// Fila de parametros de um ciclo
	private static final Queue<CycleParams> paramsQueue = new LinkedList<CycleParams>();
	
	// Função auxiliar para inserir varios passos iguais na fila
	private static void enqueueParams(CycleParams cp, int cnt) {
		for (int i = 0; i < cnt; i++)
			paramsQueue.add(cp);
	}
	
	// Enfileira o movimento de rotação do sensor ultrassonico
	private static void enqueueUltrasonicRotate() {
		ultrasonicDistLeft = 0;
		ultrasonicDistRight = 0;
		enqueueParams(new CycleParams(0, -Config.ULTRASONIC_ROTATION_STEP, 0), Config.ULTRASONIC_ROTATION_CNT);
		if (Config.ULTRASONIC_ROTATION_REMINDER != 0)
			enqueueParams(new CycleParams(0, -Config.ULTRASONIC_ROTATION_STEP 
					* Config.ULTRASONIC_ROTATION_REMINDER, 0), 1);		
	}
	
	// Checa se sensor ultrassonico está nos limites do arco, e salva o valor do sensor caso ele esteja.
	// variáveis ultrasonicDistLeft e ultrasonicDistRight terão os valores no futuro.
	private static void ultrasonicCheck() {
		if (ultrasonicDistLeft == 0 && speed == 0) {
			ultrasonicDistLeft = ultrasonicDist;
			enqueueParams(new CycleParams(0, Config.ULTRASONIC_ROTATION_STEP, 0), Config.ULTRASONIC_ROTATION_CNT * 2);
			if (Config.ULTRASONIC_ROTATION_REMINDER != 0)
				enqueueParams(new CycleParams(0, Config.ULTRASONIC_ROTATION_STEP 
						* Config.ULTRASONIC_ROTATION_REMINDER, 0), 1 * 2);		
		} else if (ultrasonicDistRight == 0 && speed == 0) { 
			ultrasonicDistRight = ultrasonicDist;
			enqueueParams(new CycleParams(0, -Config.ULTRASONIC_ROTATION_STEP, 0), Config.ULTRASONIC_ROTATION_CNT);
			if (Config.ULTRASONIC_ROTATION_REMINDER != 0)
				enqueueParams(new CycleParams(0, -Config.ULTRASONIC_ROTATION_STEP 
						* Config.ULTRASONIC_ROTATION_REMINDER, 0), 1);		
		}
	}
	
	// Enfileira uma rotação do carro no sentido anti-horario
	private static void rotateLeft() {
		enqueueParams(new CycleParams(-Config.CAR_ROTATION_STEP, 0, 0), Config.CAR_ROTATION_CNT);
		if (Config.CAR_ROTATION_REMINDER != 0)
			enqueueParams(new CycleParams(-Config.CAR_ROTATION_STEP * Config.CAR_ROTATION_REMINDER, 0, 0), 1);
		enqueueParams(new CycleParams(0, 0, Config.CAR_SPEED), 1);
	}
	
	// Enfileira uma rotação do carro no sentido horario
	private static void rotateRight() {
		enqueueParams(new CycleParams(Config.CAR_ROTATION_STEP, 0, 0), Config.CAR_ROTATION_CNT);
		if (Config.CAR_ROTATION_REMINDER != 0)
			enqueueParams(new CycleParams(Config.CAR_ROTATION_STEP * Config.CAR_ROTATION_REMINDER, 0, 0), 1);
		enqueueParams(new CycleParams(0, 0, Config.CAR_SPEED), 1);
	}
	
	/*
	 * Execução de 1 ciclo da simulação. Isso consiste de duas partes, execucão de 1 ciclo do
	 * executável do arduino (função loop), e execução de 1 ciclo de transformações dos objetos 
	 * geométricos que representam o carro e seus sensores. As transformações geométricas alteram
	 * a posição do carro e seus sensores, o que dá o efeito de animação na tela.
	 */
	public static void oneCycle() {
		if (carRect == null)
			return;
		
		CycleParams params = null;
		if (!paramsQueue.isEmpty()) {
			// paramsQueue não está vazia. Isso significa que existe um passo enfileirado ali,
			// e esse passo será retirado da fila e executado nesse bloco.
			params = paramsQueue.remove();
			
			// gira o carro
			if (params.rotation != 0) {
				direction = getDirectionVal(direction, params.rotation);
				AffineTransform at = AffineTransform.getRotateInstance(params.rotation, 
						carRect.getBounds2D().getCenterX(), carRect.getBounds2D().getCenterY());
				carRect.transform(at);
			}
			// gira sensor ultrassonico
			if (params.ultrasonicRotation != 0) {
				ultrasonicDirection = getDirectionVal(ultrasonicDirection, params.ultrasonicRotation);
				double[] p1 = getFirstMoveTo(ultrasonicLine);
				AffineTransform at = AffineTransform.getRotateInstance(
						params.ultrasonicRotation, p1[0], p1[1]);
				ultrasonicLine.transform(at);
			}
			// altera velocidade do carro
			speed = params.speed;		
		}
		
		// translada o carro SPEED PIXELS para frente. (por padrão é 1)
		double x = 0;
		double y = -speed;
		AffineTransform at = AffineTransform.getTranslateInstance(
				x * Math.cos(direction) - y * Math.sin(direction),
				x * Math.sin(direction) + y * Math.cos(direction));
		
		// Com sistema de colisão desligado, movimento é feito diretamente na variável
		// carRect. Isso significa que o carro pode passar em cima dos blocos pretos.
		if (!Config.CAR_COLLISION_CHECK_ON)
			carRect.transform(at);
							
		if (Config.CAR_COLLISION_CHECK_ON) {
			// Sistema de checagem de colisão ligado, isso causa um código mais complicado.
			// Aqui o que é acontece é que o movimento do carro é feito 
			// em uma variável temporária carT. Caso o movimento na variável
			// temporária não resulte em colisão, carRect = carT, ou seja,
			// o carro agora será carT, pois o movimento não causou colisão.
			// Caso o movimento em carT resulte colisão, será feito um movimento diferente para
			// desvio da colisão, e carRect será o carro do ciclo passado + movimento de desvio.
					
			// Pegando todo os pontos do retangulo do carro (as 4 quinas)
			Path2D carT = (Path2D)at.createTransformedShape(carRect);
			PathIterator pi = carT.getPathIterator(null);
			double[][] points = new double[4][2];
			int i = 0;
			while (!pi.isDone()) {
				double[] coords = new double[6];
				int segType = pi.currentSegment(coords);
				if (segType == PathIterator.SEG_MOVETO) {
					points[i][0] = coords[0];
					points[i][1] = coords[1];
					i++;
				}
				pi.next();
			}
			// checando as linhas do retangulo do carro, se elas estão tocando
			// em um pixel preto.
			int[] arr = bresenham((int) points[0][0], (int) points[0][1], 
					(int) points[1][0], (int) points[1][1]);
			boolean frontCollision = arr[0] != (int) points[1][0] 
					|| arr[1] != (int) points[1][1];
			arr = bresenham((int) points[1][0], (int) points[1][1], 
					(int) points[2][0], (int) points[2][1]);
			boolean rightCollision = arr[0] != (int) points[2][0] 
					|| arr[1] != (int) points[2][1];
			arr = bresenham((int) points[2][0], (int) points[2][1], 
					(int) points[3][0], (int) points[3][1]);
			boolean backCollision = arr[0] != (int) points[3][0] 
					|| arr[1] != (int) points[3][1];
			arr = bresenham((int) points[3][0], (int) points[3][1], 
					(int) points[0][0], (int) points[0][1]);
			boolean leftCollision = arr[0] != (int) points[0][0] 
					|| arr[1] != (int) points[0][1];
			
			if (frontCollision || rightCollision || backCollision || leftCollision) {
				// qualquer colisão exige uma parada do carro e um desvio, mas cada
				// colisão tem seu desvio especifico.
				paramsQueue.clear();
				collisionCnt++;
				if (frontCollision) {
					at = AffineTransform.getRotateInstance(direction, points[0][0], points[0][1]);
					Point2D p1 = new Point2D.Double(points[0][0], points[0][1] - 20);
					at.transform(p1, p1);
					arr = bresenham((int) points[0][0], (int) points[0][1], 
							(int) p1.getX(), (int) p1.getY());
					double distP1 = Point2D.distance(points[0][0], points[0][1], arr[0], arr[1]);
					
					at = AffineTransform.getRotateInstance(direction, points[1][0], points[1][1]);
					Point2D p2 = new Point2D.Double(points[1][0], points[1][1] - 20);
					at.transform(p2, p2);
					arr = bresenham((int) points[1][0], (int) points[1][1], 
							(int) p2.getX(), (int) p2.getY());
					double distP2 = Point2D.distance(points[1][0], points[1][1], arr[0], arr[1]);
					
					if (distP1 >= distP2)
						enqueueParams(new CycleParams(-Config.CAR_COLLISION_STEP, 0, 0), 1);
					else
						enqueueParams(new CycleParams(Config.CAR_COLLISION_STEP, 0, 0), 1);
				}
				if (rightCollision) {
					double dir = getDirectionVal(direction, -Math.toRadians(90));
					at = AffineTransform.getTranslateInstance(
							x * Math.cos(dir) - y * Math.sin(dir),
							x * Math.sin(dir) + y * Math.cos(dir));
					carRect.transform(at);
				}
				if (leftCollision) {
					double dir = getDirectionVal(direction, Math.toRadians(90));
					at = AffineTransform.getTranslateInstance(
							x * Math.cos(dir) - y * Math.sin(dir),
							x * Math.sin(dir) + y * Math.cos(dir));
					carRect.transform(at);
				}
				enqueueParams(new CycleParams(0, 0, Config.CAR_SPEED), 1);
			} else {
				carRect = carT;
			}
		}
		
		// Linha que representa o sensor infravermelho esquerdo
		double[] leftInfraredP1 = getFirstMoveTo(carRect);
		Point2D leftInfraredP2 = new Point2D.Double(leftInfraredP1[0], leftInfraredP1[1] - Config.INFRARED_RANGE);
		at = AffineTransform.getRotateInstance(direction + Config.INFRARED_ANGLE_LEFT, leftInfraredP1[0], leftInfraredP1[1]);
		at.transform(leftInfraredP2, leftInfraredP2);
		int[] arr = bresenham((int) leftInfraredP1[0], (int) leftInfraredP1[1], 
				(int) leftInfraredP2.getX(), (int) leftInfraredP2.getY()); 
		leftInfraredLine.reset();
		leftInfraredLine.moveTo(leftInfraredP1[0], leftInfraredP1[1]);
		leftInfraredLine.lineTo(arr[0], arr[1]);
		leftInfraredLine.closePath();
		leftInfraredDist = Config.CAR_SENSOR_ON 
				? Point2D.distance(leftInfraredP1[0], leftInfraredP1[1], arr[0], arr[1])
				: Double.MAX_VALUE;
		
		// Linha que representa sensor infravermelho direito
		double[] rightInfraredP1 = getSecondMoveTo(carRect);
		Point2D rightInfraredP2 = new Point2D.Double(rightInfraredP1[0], rightInfraredP1[1] - Config.INFRARED_RANGE);
		at = AffineTransform.getRotateInstance(direction + Config.INFRARED_ANGLE_RIGHT, rightInfraredP1[0], rightInfraredP1[1]);
		at.transform(rightInfraredP2, rightInfraredP2);
		arr = bresenham((int) rightInfraredP1[0], (int) rightInfraredP1[1], 
				(int) rightInfraredP2.getX(), (int) rightInfraredP2.getY());
		rightInfraredLine.reset();
		rightInfraredLine.moveTo(rightInfraredP1[0], rightInfraredP1[1]);
		rightInfraredLine.lineTo(arr[0], arr[1]);
		rightInfraredLine.closePath();
		rightInfraredDist = Config.CAR_SENSOR_ON
				? Point2D.distance(rightInfraredP1[0], rightInfraredP1[1], arr[0], arr[1])
				: Double.MAX_VALUE;
		
		// Linha que representa o sensor ultrassonico
		double[] ultrasonicP1 = new double[2];
		ultrasonicP1[0] = (leftInfraredP1[0] + rightInfraredP1[0]) / 2;
		ultrasonicP1[1] = (leftInfraredP1[1] + rightInfraredP1[1]) / 2;
		Point2D ultrasonicP2 = new Point2D.Double(ultrasonicP1[0], ultrasonicP1[1] - Config.ULTRASONIC_RANGE);
		at = AffineTransform.getRotateInstance(direction + ultrasonicDirection, ultrasonicP1[0], ultrasonicP1[1]);
		at.transform(ultrasonicP2, ultrasonicP2);
		arr = bresenham((int) ultrasonicP1[0], (int) ultrasonicP1[1], (int) ultrasonicP2.getX(),
				(int) ultrasonicP2.getY());
		ultrasonicLine.reset();
		ultrasonicLine.moveTo(ultrasonicP1[0], ultrasonicP1[1]);
		ultrasonicLine.lineTo(arr[0], arr[1]);
		ultrasonicLine.closePath();
		ultrasonicDist = Config.CAR_SENSOR_ON
				? Point2D.distance(ultrasonicP1[0], ultrasonicP1[1], arr[0], arr[1])
				: Double.MAX_VALUE;
		
		if (!paramsQueue.isEmpty())
			return;
		
		ultrasonicCheck();
				
		if (leftInfraredDist < Config.INFRARED_THRESHOLD && rightInfraredDist > Config.INFRARED_THRESHOLD
			&& speed != 0) {
			enqueueParams(new CycleParams(Config.CAR_ROTATION_STEP, 0, speed), 1);
		} else if (rightInfraredDist < Config.INFRARED_THRESHOLD && leftInfraredDist > Config.INFRARED_THRESHOLD
					&& speed != 0) {
			enqueueParams(new CycleParams(-Config.CAR_ROTATION_STEP, 0, speed), 1);
		}

		// CICLO DO ARDUINO COMEÇA AQUI. TODO CÓDIDO ACIMA É REFERENTE AO MOVIMENTO DO CARRO E SEUS SENSORES
		// NO MUNDO. COMO SE FOSSE A FÍSICA DOS OBJETOS. DAQUI PRA FRENTE, O CÓDIGO REPRESENTA O EXECUTÁVEL
		// NA MEMÓRIA DO ARDUINO.
		
		if (speed != 0 && ultrasonicDist < Config.ULTRASONIC_THRESHOLD) {
			enqueueUltrasonicRotate();
		}
		
		if (speed == 0 && ultrasonicDistLeft != 0 && ultrasonicDistRight != 0) {
			if (ultrasonicDistLeft >= ultrasonicDistRight) {
				rotateLeft();
			} else {
				rotateRight();
			}
		}
	}
	
	// Inicializa variáveis dessa classe, com o carro na posição x e y
	public static void init(int x, int y) {
		direction = getDirectionVal(0, Config.CAR_DIRECTION);
		speed = Config.CAR_SPEED;
		collisionCnt = 0;
		carRect = new Path2D.Double();
		double halfW = Config.CAR_WIDTH / 2;
		double halfH = Config.CAR_HEIGHT / 2;
		carRect.moveTo(x - halfW, y - halfH);
		carRect.lineTo(x + halfW, y - halfH);
		carRect.moveTo(x + halfW, y - halfH);
		carRect.lineTo(x + halfW, y + halfH);
		carRect.moveTo(x + halfW, y + halfH);
		carRect.lineTo(x - halfW, y + halfH);
		carRect.moveTo(x - halfW, y + halfH);
		carRect.lineTo(x - halfW, y - halfH);
		carRect.closePath();
		AffineTransform at = AffineTransform.getRotateInstance(direction, 
				carRect.getBounds2D().getCenterX(), carRect.getBounds2D().getCenterY());
		carRect.transform(at);
		
		paramsQueue.clear();
		leftInfraredLine = new Path2D.Double();
		rightInfraredLine = new Path2D.Double();
		ultrasonicLine = new Path2D.Double();
		ultrasonicDirection = 0;
	}
	
	/*
	 * getters para desenhar no DrawingPanel
	 */
	
	public static Path2D getUltrasonicLine() {
		return ultrasonicLine;
	}

	public static Path2D getCarRect() {
		return carRect;
	}

	public static Path2D getLeftInfraredLine() {
		return leftInfraredLine;
	}

	public static Path2D getRightInfraredLine() {
		return rightInfraredLine;
	}

	// Retorna o primeiro ponto do Path2D p. 
	// Isso é usado para pegar a quina frontal-esquerda do retangulo do carro. 
	// o retorno double[] é o valor x,y da quina frontal-esquerda do carro.
	private static double[] getFirstMoveTo(Path2D p) {
		PathIterator pi = p.getPathIterator(null);
		double[] point = new double[2];
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int segType = pi.currentSegment(coords);
			if (segType == PathIterator.SEG_MOVETO) {
				point[0] = coords[0];
				point[1] = coords[1];
				return point;
			}
			pi.next();
		}
		return null;
	}

	// Retorna o segundo ponto do Path2D p.
	// Isso é usado para pegar a quina frontal-direita do retangulo do carro.
	// o retorno double[] é o valor x,y da quina frontal-direita do carro.
	private static double[] getSecondMoveTo(Path2D p) {
		PathIterator pi = p.getPathIterator(null);
		double[] point = new double[2];
		int cnt = 0;
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int segType = pi.currentSegment(coords);
			if (segType == PathIterator.SEG_MOVETO) {
				if (cnt == 1) {
					point[0] = coords[0];
					point[1] = coords[1];
					return point;
				}
				cnt++;
			}
			pi.next();
		}
		return null;
	}
	
	// Incremento circular de theta graus radianos, sobre o valor direction. 
	// O valor sempre varia entre 0 e 2PI.
	// Essa funçao não altera valor de nada, ela apenas calcula o novo valor, e
	// a atribuição do novo valor deve ser feita por quem a chamou. Exemplo:
	// direction = getDirectionVal(direction, params.rotation);
	private static double getDirectionVal(double direction, double theta) {
		if (Math.signum(theta) > 0) 
			return (direction + theta) % (2 * Math.PI);
		else {
			if (direction + theta > 0)
				return direction + theta;
			else
				return (2 * Math.PI) + (direction + theta);
		}
	}
	
	// Algoritmo de bresenham, usado para traçar os pontos da reta entre 2 pontos passados de parametro.
	// Função tenta tocar em todos os pontos da reta entre os pontos x1,y1 e x2,y2; sem passar por nenhum
	// pixel preto. Função quebra o loop ao encontrar um pixel preto, e retorna o último ponto válido da reta.
	// A idéia aqui é descobrir qual a reta máxima possível de ser traçada entre 2 pontos, sem tocar em nenhum
	// pixel preto. Com isso, podemos simular a entrada dos sensores e checar colisões físicas do carro em uma parede.
	private static int[] bresenham(int x1, int y1, int x2, int y2) {
		// delta of exact value and rounded value of the dependent variable
		int d = 0;
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int dx2 = 2 * dx; // slope scaling factors to
		int dy2 = 2 * dy; // avoid floating point
		int ix = x1 < x2 ? 1 : -1; // increment direction
		int iy = y1 < y2 ? 1 : -1;
		int x = x1;
		int y = y1;

		if (dx >= dy) {
			while (true) {
				if (World.checkPoint(x, y) || x == x2)
					break;
				x += ix;
				d += dy2;
				if (d > dx) {
					y += iy;
					d -= dx2;
				}
			}
		} else {
			while (true) {
				if (World.checkPoint(x, y) || (y == y2))
					break;
				y += iy;
				d += dx2;
				if (d > dy) {
					x += ix;
					d -= dy2;
				}
			}
		}
		
		int[] ret = {x, y};
		return ret;
	}
	
}
