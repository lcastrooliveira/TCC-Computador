
import grafo.Grafo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import algoritmos.AlgBusca;
import algoritmos.BuscaEmLargura;
import algoritmos.BuscaEmProfundidade;
import algoritmos.Dijkstra;
/**
 * 
 * @author Lucas
 * @version 1.0
 * Ultima atualizacao em: 25/02/2016
 */
public class Main {
	
	/**
	 * Codigo das direcoes, que foi mantido igual ao
	 * que foi programado no robo
	 */
	static final int NORTE = 13;
	static final int OESTE = 37;
	static final int LESTE = 59;
	static final int SUL = 29;

	/**
	 * Porta que vai receber os dados via serial
	 * O resultado sera armazenado numa StringBuffer
	 * e convertido para uma String mais tarde
	 */
	static SerialPort serialPort;
	static StringBuffer buffer;
	
	/**
	 * grid - representa a matriz auxiliar que abstrai
	 * o cenario. Linha, coluna representam as dimensoes da matriz
	 * que sera contruida em grid, uma vez que os dados sejam
	 * recebidos do robo. Grafo vai armazenar as conexoes de cada
	 * quadrado do cenario com os seus vizinhos.
	 */
	static char[][] grid;
	static int linha,coluna;
	static Grafo grafo;
	
	/**
	 * Armazena uma referencia para cada algoritimo de busca
	 */
	static BuscaEmProfundidade dfs;
	static BuscaEmLargura bfs;
	static Dijkstra djk;
	
	/**
	 * Input do usuario que informa qual o algoritmo deseja 
	 * escolher
	 */
	static String algoritmoSelecionado;
	
	/**
	 * Vetor de coordenadas que sera mandado de volta ao robo
	 */
	static byte[] coordenadas;
	
	/**
	 * Ler do teclado
	 */
	static BufferedReader in = new BufferedReader (
            new InputStreamReader (System.in));
	
	/**
	 * Metodo main, aqui e onde o programa se inicia
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Digite uma letra para especificar qual algoritmo deseja utilizar:");
			System.out.println("D - Busca em Profundidade");
			System.out.println("B - Busca em Largura");
			System.out.println("K - Dijkstra");
			System.out.print("R: ");
			algoritmoSelecionado = in.readLine();
			System.out.println(algoritmoSelecionado);
			System.out.println("Esperando Mapa da Grid.....");
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static AlgBusca executarAlgoritmo(String algoritmo) throws Exception {
		int ultimaPosicao = (linha*coluna)-1;
		if(algoritmo.equals("D")) {
			dfs = new BuscaEmProfundidade (grafo);
		    dfs.buscaEmProfundidade (0,ultimaPosicao);
		    return dfs;
		} else if(algoritmo.equals("B")) {
			bfs = new BuscaEmLargura(grafo);
			bfs.buscaEmLargura(0,ultimaPosicao);
			return bfs;
		} else if(algoritmo.equals("K")) {
			djk = new Dijkstra(grafo);
			djk.obterArvoreCMC(0);
			djk.imprimeCaminho(19, 0);
			return djk;
		}
		throw new Exception("Algoritmo nao reconhecido");
	}

	private static void construirCoordenadas(AlgBusca algoritmo) {
		int caminhoArray[] = new int[grafo.numVertices()];
	    int antecessor = grafo.numVertices() - 1;
	    caminhoArray[0] = antecessor;
	    for(int i = 1; i < grafo.numVertices(); i++) {
	    	if(antecessor == -1) break;
	    	caminhoArray[i] = algoritmo.antecessor(antecessor);
		    antecessor = algoritmo.antecessor(antecessor);
	    }
	    System.out.println("Caminho Encontrado");
	    for(int i = 0; i < caminhoArray.length; i++) {
	    	if(caminhoArray[i] == -1) break;
	    	System.out.print(caminhoArray[i]+" ");
	    }
	    coordenadas = new byte[caminhoArray.length];
	    System.out.println();
	    System.out.println("Em coordenadas:");
	    for(int i = 0; i < caminhoArray.length -1; i++) {
	    	int de = caminhoArray[i];
	    	int para = caminhoArray[i+1];
	    	if(para == -1) break;
	    	if(para == de -1) {
	    		System.out.print("O ");
	    		coordenadas[i] = OESTE;
	    	} else if(para == de + 1) {
	    		System.out.print("L ");
	    		coordenadas[i] = LESTE;
	    	} else if(para == de + coluna) {
	    		System.out.print("N ");
	    		coordenadas[i] = NORTE;
	    	} else if(para == de - coluna) {
	    		System.out.print("S ");
	    		coordenadas[i] = SUL;
	    	}
	    }
	    System.out.println();
	    
	}

	/**
	 * Abre a porta serial e fica esperando os dados chegarem do robo
	 * A comunicacao com a porta serial e orientada a eventos, representado pelo
	 * SerialPortReader()
	 */
	public static void start() {
		buffer = new StringBuffer();
		serialPort = new SerialPort("COM6"); 
		try {
			serialPort.openPort();//Open port
			serialPort.setParams(9600, 8, 1, 0);//Set params
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
			serialPort.setEventsMask(mask);//Set mask
			serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
		}
		catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Metodo descobre as dimensoes da matriz, logo em seguida constroi a grid,
	 * o grafo e executa o algoritmo. Depois que o a saida do algoritmo preenche o
	 * vetor de coordenadas, envia de volta ao robo via conexao serial abstradia pelo zig bee
	 * @param mensagem
	 * @throws Exception
	 */
	public static void processarMensagem(String mensagem) throws Exception {
		System.out.println("Curva:");
		String mapeamento = mensagem.substring(0,mensagem.length()-2);
		coluna = (short)mensagem.charAt(mensagem.length()-2);
		linha = mapeamento.length() / coluna;
		System.out.println(coluna);
		System.out.println("String Recebida:");
		System.out.println(mapeamento);
		System.out.println("tamanho: "+mapeamento.length());
		System.out.println("Pronto para processar");
		constroiGrid(coluna, mapeamento);
		imprimeGrid();
		constroiGrafo();
		AlgBusca algoritimo = executarAlgoritmo(algoritmoSelecionado);
		construirCoordenadas(algoritimo);
		enviarCoordenadas();
	}
	
	public static void constroiGrid(int curva, String mapeamento) {
		int pos = 0;
		int coluna = curva;
		int linha = mapeamento.length() / coluna;
		//Transformar a string numa matriz para tornar a grid mais clara
		char grid[][] = new char[linha][coluna];
		for(int i = 0; i < linha; i++) {
			if(i % 2 == 0) {
				for(int j = 0; j < coluna; j++) {
					grid[i][j] = mapeamento.charAt(pos);
					pos++;
				}
			} else {
				for(int j = coluna -1; j >= 0; j--) {
					grid[i][j] = mapeamento.charAt(pos);
					pos++;
				}
			}
		}
		Main.grid = grid;
		Main.linha = linha;
		Main.coluna = coluna;
	}
	
	public static void imprimeGrid() {
		System.out.print("X\t");
		for(int i = 0; i < coluna; i++) {
			System.out.print(i+"\t");
		}
		System.out.println();
		for(int i = 0; i < linha; i++) {
			System.out.print(i+"\t");
			for(int j = 0; j < coluna; j++) {
				System.out.print(grid[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	public static void constroiGrafo() {
		Grafo grafo = new Grafo(linha*coluna);
		Main.grafo = grafo;
		for(int i = 0; i < linha; i++) {
			for(int j = 0; j < coluna; j++) {
				inserirArestaEsquerda(i,j);
				inserirArestaDireita(i,j);
				inserirArestaSuperior(i,j);
				inserirArestaInferior(i,j);
			}
		}
		System.out.println("Grafo:");
		grafo.imprime();
	}
	
	public static int to1D(int i, int j) {
		return i*coluna+j;
	}
	
	public static void inserirArestaEsquerda(int i, int j) {
		if(grid[i][j] == 'P') return; //no preto nao tem aresta
		if(j - 1 < 0) return; //borda
		if(grid[i][j-1] == 'B') {
			grafo.insereAresta(to1D(i, j), to1D(i, j-1), 1);
		}
	}
	
	public static void inserirArestaDireita(int i, int j) {
		if(grid[i][j] == 'P') return; //no preto nao tem aresta
		if(j + 1 >= coluna) return; //borda
		if(grid[i][j+1] == 'B') {
			grafo.insereAresta(to1D(i, j), to1D(i, j+1), 1);
		}
	}
	
	public static void inserirArestaSuperior(int i, int j) {
		if(grid[i][j] == 'P') return; //no preto nao tem aresta
		if(i + 1 >= linha) return; //borda
		if(grid[i+1][j] == 'B') {
			grafo.insereAresta(to1D(i, j), to1D(i+1, j), 1);
		}
	}
	
	public static void inserirArestaInferior(int i, int j) {
		if(grid[i][j] == 'P') return; //no preto nao tem aresta
		if(i - 1 < 0) return; //borda
		if(grid[i-1][j] == 'B') {
			grafo.insereAresta(to1D(i, j), to1D(i-1, j), 1);
		}
	}

	public static void enviarCoordenadas() {
		System.out.println("Mandando Coordenadas de Volta:");
		try {
			for(byte b : coordenadas) System.out.print(b+" ");
            serialPort.writeBytes(coordenadas);//Write data to port
            serialPort.closePort();//Close serial port
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
	}

	/**
	 * In this class must implement the method serialEvent, through it we learn about 
	 * events that happened to our port. But we will not report on all events but only 
	 * those that we put in the mask. In this case the arrival of the data and change the 
	 * status lines CTS and DSR
	 * 
	 * Modificado de: http://java-simple-serial-connector.googlecode.com/svn-history/r6/wiki/jSSC_examples.wiki
	 */
	static class SerialPortReader implements SerialPortEventListener {
		
		public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR()){//If data is available
				try {
					/**
					 * Vai concatenando qualquer dado que receber no string buffer
					 * quando receber o caracter 'x' significa que acabou a mensagem, chamando
					 * o metodo processarMensagem()
					 */
					buffer.append(serialPort.readString());
					if(buffer.toString().endsWith("x")) {
						try {
							System.out.println("Dados da Grid Recebidos :)");
							processarMensagem(buffer.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				}
				catch (SerialPortException ex) {
					System.out.println(ex);
				}
			}
			else if(event.isCTS()){//If CTS line has changed state
				if(event.getEventValue() == 1){//If line is ON
					System.out.println("CTS - ON");
				}
				else {
					System.out.println("CTS - OFF");
				}
			}
			else if(event.isDSR()){///If DSR line has changed state
				if(event.getEventValue() == 1){//If line is ON
					System.out.println("DSR - ON");
				}
				else {
					System.out.println("DSR - OFF");
				}
			}
		}
	}
}
