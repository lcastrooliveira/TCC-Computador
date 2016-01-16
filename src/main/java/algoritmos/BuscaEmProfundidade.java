package algoritmos;

import grafo.Grafo;

/**
 * @author Ziviani
 * Retirado de:
 * http://www2.dcc.ufmg.br/livros/algoritmos-java/implementacoes.php
 */

public class BuscaEmProfundidade implements AlgBusca {
	
  public static final byte branco = 0;
  public static final byte cinza  = 1;
  public static final byte preto  = 2;
  private int descoberta[], termino[], antecessor[];
  private Grafo grafo;
  
  public BuscaEmProfundidade (Grafo grafo) {
    this.grafo = grafo; 
    int numVertices = this.grafo.numVertices();
    descoberta = new int[numVertices]; 
    termino = new int[numVertices]; 
    antecessor = new int[numVertices];
  }
  
  private int visitaDfs (int u, int tempo, int cor[]) {
    cor[u] = cinza; 
    this.descoberta[u] = ++tempo;
    if (!this.grafo.listaAdjVazia (u)) {
      Grafo.Aresta a = this.grafo.primeiroListaAdj (u);
      while (a != null) {
        int v = a.v2 ();
        if (cor[v] == branco) {
          this.antecessor[v] = u;
          tempo = this.visitaDfs (v, tempo, cor);
        }
        a = this.grafo.proxAdj (u);
      }
    }
    cor[u] = preto; this.termino[u] = ++tempo;
    return tempo;
  }
  
  private int visitaDfs (int origem, int destino, int tempo, int cor[]) {
	    cor[origem] = cinza; 
	    this.descoberta[origem] = ++tempo;
	    if (!this.grafo.listaAdjVazia (origem)) {
	      Grafo.Aresta a = this.grafo.primeiroListaAdj (origem);
	      while (a != null) {
	        int v = a.v2 ();
	        if (cor[v] == branco) {
	          this.antecessor[v] = origem;
	          //Nao precisa processar o grafo inteiro, se achou o destino, ja aborta
	          //a execucao. Neste cenario, os valores do array termino nao sao relevantes
	          if(v == destino) return tempo;
	          tempo = this.visitaDfs (v, tempo, cor);
	        }
	        a = this.grafo.proxAdj (origem);
	      }
	    }
	    cor[origem] = preto; this.termino[origem] = ++tempo;
	    return tempo;
	  }
  
  public void buscaEmProfundidade () {
    int tempo = 0; 
    int cor[] = new int[this.grafo.numVertices ()]; 
    for (int u = 0; u < grafo.numVertices (); u++) {
      cor[u] = branco; 
      this.antecessor[u] = -1;
    }     
    for (int u = 0; u < grafo.numVertices (); u++)
      if (cor[u] == branco) 
    	  tempo = this.visitaDfs (u, tempo, cor);
  }
  
  public int buscaEmProfundidade(int origem, int destino) {
	  int tempo = 0; 
	    int cor[] = new int[this.grafo.numVertices ()];
	    //inicializa o grafo
	    for (int u = 0; u < grafo.numVertices (); u++) {
	      cor[u] = branco; 
	      this.antecessor[u] = -1;
	    }   
	    tempo = this.visitaDfs (origem, destino, tempo, cor);
	    return tempo;
  }
  
  public void imprimeCaminho(int origem, int destino) {
	  if(origem == destino) {
		  System.out.print("\t"+origem);
	  } else if(this.antecessor[destino] == -1) {
		  System.out.println("Nao existe caminho da origem "+origem+" ate o destino: "+destino);
	  } else {
		  imprimeCaminho(origem, this.antecessor[destino]);
		  System.out.print("\t"+destino);
	  }
  }
  
  public int d (int v) { 
	  return this.descoberta[v]; 
  }
  
  public int t (int v) { 
	  return this.termino[v]; 
  }
  
  public int antecessor (int v) { 
	  return this.antecessor[v]; 
  }
}
