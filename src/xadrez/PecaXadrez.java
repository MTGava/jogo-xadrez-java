package xadrez;

import camadaTabuleiro.Peca;
import camadaTabuleiro.Tabuleiro;

public class PecaXadrez extends Peca {

	private Cor cor;

	public PecaXadrez(Tabuleiro tabuleiro, Cor cor) {
		super(tabuleiro);
		this.cor = cor;
	}

	public Cor getCor() {
		return cor;
	}

}
