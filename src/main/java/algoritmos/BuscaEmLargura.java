package algoritmos;

import estruturasAuxiliares.Fila;
import grafo.Grafo;

/**
 * @author Ziviani
 * Retirado de:
 * http://www2.dcc.ufmg.br/livros/algoritmos-java/implementacoes.php
 */
public class BuscaEmLargura implements AlgBusca {
  public static final byte branco = 0;
  public static final byte cinza  = 1;
  public static final byte preto  = 2;
  private int d[], antecessor[];
  private Grafo grafo;
  public BuscaEmLargura (Grafo grafo) {
    this.grafo = grafo;
    int n = this.grafo.numVertices();
    this.d = new int[n]; 
    this.antecessor = new int[n];
  }
  
  private void visitaBfs (int u, int cor[]) throws Exception {
    cor[u] = cinza; this.d[u] = 0;
    Fila fila = new Fila (); 
    fila.enfileira (new Integer (u));
    while (!fila.vazia ()) {
      Integer aux = (Integer)fila.desenfileira (); 
      u = aux.intValue(); 
      if (!this.grafo.listaAdjVazia (u)) {
        Grafo.Aresta a = this.grafo.primeiroListaAdj (u);
        while (a != null) {
          int v = a.v2 ();
          if (cor[v] == branco) {
            cor[v] = cinza; 
            this.d[v] = this.d[u] + 1;
            this.antecessor[v] = u; 
            fila.enfileira (new Integer (v));
          }
          a = this.grafo.proxAdj (u);
        }
      }
      cor[u] = preto;
    }
  }
  
  private void visitaBfs (int origem, int destino, int cor[]) throws Exception {
	    cor[origem] = cinza; this.d[origem] = 0;
	    Fila fila = new Fila (); 
	    fila.enfileira (new Integer (origem));
	    while (!fila.vazia ()) {
	      Integer aux = (Integer)fila.desenfileira (); 
	      origem = aux.intValue(); 
	      if (!this.grafo.listaAdjVazia (origem)) {
	        Grafo.Aresta a = this.grafo.primeiroListaAdj (origem);
	        while (a != null) {
	          int v = a.v2 ();
	          if (cor[v] == branco) {
	            cor[v] = cinza; 
	            this.d[v] = this.d[origem] + 1;
	            this.antecessor[v] = origem; 
	            fila.enfileira (new Integer (v));
	            //Nao precisa processar o grafo inteiro, se achou o destino, ja aborta
		        //a execucao. Neste cenario, os valores do array termino nao sao relevantes
		          if(v == destino) return;
	          }
	          a = this.grafo.proxAdj (origem);
	        }
	      }
	      cor[origem] = preto;
	    }
	  }
  
  public void buscaEmLargura () throws Exception {
    int cor[] = new int[this.grafo.numVertices ()]; 
    for (int u = 0; u < grafo.numVertices (); u++) {
      cor[u] = branco; 
      this.d[u] = Integer.MAX_VALUE; 
      this.antecessor[u] = -1;
    }     
    for (int u = 0; u < grafo.numVertices (); u++)
      if (cor[u] == branco) this.visitaBfs (u, cor);
  }
  
  public void buscaEmLargura (int origem, int destino) throws Exception {
	    int cor[] = new int[this.grafo.numVertices ()]; 
	    //Inicializa grafo
	    for (int u = 0; u < grafo.numVertices (); u++) {
	      cor[u] = branco; 
	      this.d[u] = Integer.MAX_VALUE; 
	      this.antecessor[u] = -1;
	    }     
	    this.visitaBfs (origem,destino, cor);
	  }
  
  public int d (int v) { return this.d[v]; }
  public int antecessor (int v) { return this.antecessor[v]; }  
  public void imprimeCaminho (int origem, int v) {
    if (origem == v) System.out.println (origem);
    else if (this.antecessor[v] == -1) 
      System.out.println ("Nao existe caminho de " + origem + " ate " + v);
    else {
      imprimeCaminho (origem, this.antecessor[v]);
      System.out.println (v);
    }    
  }
}