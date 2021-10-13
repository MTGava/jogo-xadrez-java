package camadaTabuleiro;

public class Tabuleiro {

	private int linhas;
	private int colunas;
	private Peca[][] pecas;

	public Tabuleiro(int linhas, int colunas) {
		if (linhas < 1 || colunas < 1) {
			throw new TabuleiroException("Erro ao criar o tabuleiro! Eh necessario que haja 1 linha e 1 coluna");
		}
		this.linhas = linhas;
		this.colunas = colunas;
		pecas = new Peca[linhas][colunas];
	}

	public int getLinhas() {
		return linhas;
	}

	public int getColunas() {
		return colunas;
	}

	public Peca peca(int linha, int coluna) {
		if (!posicaoExiste(linha, coluna)) {
			throw new TabuleiroException("Nao existe essa posicao no tabuleiro!");
		}
		return pecas[linha][coluna];
	}

	public Peca peca(Posicao posicao) {
		if (!posicaoExiste(posicao)) {
			throw new TabuleiroException("Nao existe essa posicao no tabuleiro!");
		}
		return pecas[posicao.getLinha()][posicao.getColuna()];
	}

	public void posicaoPeca(Peca peca, Posicao posicao) {
		if (pecaExiste(posicao)) {
			throw new TabuleiroException("Ja existe uma peca nesta posicao: " + posicao);
		}
		pecas[posicao.getLinha()][posicao.getColuna()] = peca;
		peca.posicao = posicao;
	}

	private boolean posicaoExiste(int linha, int coluna) {
		return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
	}

	public Peca removerPeca(Posicao posicao) {

		if (!posicaoExiste(posicao)) {
			throw new TabuleiroException("Posicao nao existe no tabuleiro!");
		}
		if (peca(posicao) == null) {
			return null;
		}

		Peca aux = peca(posicao);
		aux.posicao = null;
		pecas[posicao.getLinha()][posicao.getColuna()] = null;

		return aux;
	}

	public boolean posicaoExiste(Posicao posicao) {
		return posicaoExiste(posicao.getLinha(), posicao.getColuna());
	}

	public boolean pecaExiste(Posicao posicao) {
		if (!posicaoExiste(posicao)) {
			throw new TabuleiroException("Não existe essa posição no tabuleiro!");
		}
		return peca(posicao) != null;
	}

}
