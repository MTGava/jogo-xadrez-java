package xadrez;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import camadaTabuleiro.Peca;
import camadaTabuleiro.Posicao;
import camadaTabuleiro.Tabuleiro;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorAtual;
	private Tabuleiro tabuleiro;
	private boolean xeque;

	private List<Peca> pecasNoTabuleiro = new ArrayList<>();
	private List<Peca> pecasCapturadas = new ArrayList<>();

	public PartidaXadrez() {
		tabuleiro = new Tabuleiro(8, 8);
		turno = 1;
		jogadorAtual = Cor.BRANCO;
		setupInicial();
	}

	public int getTurno() {
		return turno;
	}

	public Cor getJogadorAtual() {
		return jogadorAtual;
	}
	
	public boolean getXeque() {
		return xeque;
	}

	public PecaXadrez[][] getPecas() {
		PecaXadrez[][] mat = new PecaXadrez[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for (int i = 0; i < tabuleiro.getLinhas(); i++) {
			for (int j = 0; j < tabuleiro.getColunas(); j++) {
				mat[i][j] = (PecaXadrez) tabuleiro.peca(i, j);
			}
		}

		return mat;
	}

	public boolean[][] possiveisMovimentos(PosicaoXadrez posicaoOrigem) {
		Posicao posicao = posicaoOrigem.toPosicao();
		validarBuscaPosicao(posicao);
		return tabuleiro.peca(posicao).possiveisMovimentos();
	}

	public PecaXadrez executaPecaXadrez(PosicaoXadrez buscaPosicao, PosicaoXadrez destinoPosicao) {
		Posicao origem = buscaPosicao.toPosicao();
		Posicao destino = destinoPosicao.toPosicao();
		validarBuscaPosicao(origem);
		validarCapturaPosicao(origem, destino);
		Peca pecaCapturada = moverPeca(origem, destino);
		
		if (testeXeque(jogadorAtual)) {
			desfazerMovimento(origem, destino, pecaCapturada);
			throw new XadrezException("Voce nao pode se colocar em xeque");
		}
		
		xeque = (testeXeque(oponente(jogadorAtual))) ? true : false;
		
		proximoTurno();
		return (PecaXadrez) pecaCapturada;
	}

	private Peca moverPeca(Posicao origem, Posicao destino) {
		Peca p = tabuleiro.removerPeca(origem);
		Peca pecaCapturada = tabuleiro.removerPeca(destino);
		tabuleiro.posicaoPeca(p, destino);

		if (pecaCapturada != null) {
			pecasNoTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}

		return pecaCapturada;
	}

	private void desfazerMovimento(Posicao origem, Posicao destino, Peca pecaCapturada) {
		Peca p = tabuleiro.removerPeca(destino);
		tabuleiro.posicaoPeca(p, origem);

		if (pecaCapturada != null) {
			tabuleiro.posicaoPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasNoTabuleiro.add(pecaCapturada);
		}
	}

	private void validarBuscaPosicao(Posicao posicao) {

		if (!tabuleiro.pecaExiste(posicao)) {
			throw new XadrezException("Nao existe uma peca nesta posicao");
		}
		if (jogadorAtual != ((PecaXadrez) tabuleiro.peca(posicao)).getCor()) {
			throw new XadrezException("A peca escolhida nao eh sua");
		}
		if (!tabuleiro.peca(posicao).existeAlgumMovimento()) {
			throw new XadrezException("Nao existe movimentos possiveis para a peca de origem");
		}
	}

	private void validarCapturaPosicao(Posicao origem, Posicao destino) {
		if (!tabuleiro.peca(origem).possivelMovimento(destino)) {
			throw new XadrezException("A posicao escolhida nao pode se mover para a posicao de destino");
		}
	}

	private void proximoTurno() {
		turno++;
		jogadorAtual = (jogadorAtual == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}

	private Cor oponente(Cor cor) {
		return (cor == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}

	private PecaXadrez rei(Cor cor) {
		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez)x).getCor() == cor).collect(Collectors.toList());
		for (Peca p : lista) {
			if (p instanceof Rei) {
				return (PecaXadrez) p;
			}
		}
		throw new IllegalStateException("Nao existe o rei da cor " + cor + " no tabuleiro");
	}

	private boolean testeXeque(Cor cor) {
		Posicao posicaoRei = rei(cor).getPosicaoXadrez().toPosicao();
		List<Peca> pecasOponentes = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez)x).getCor() == oponente(cor)).collect(Collectors.toList());
		for (Peca p : pecasOponentes) {
			boolean[][] mat = p.possiveisMovimentos();
			if (mat[posicaoRei.getLinha()][posicaoRei.getColuna()]) {
				return true;
			}
		}

		return false;
	}

	private void posicaoNovaPeca(char coluna, int linha, PecaXadrez pecaXadrez) {
		tabuleiro.posicaoPeca(pecaXadrez, new PosicaoXadrez(coluna, linha).toPosicao());
		pecasNoTabuleiro.add(pecaXadrez);
	}

	private void setupInicial() {
		posicaoNovaPeca('c', 1, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('c', 2, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('d', 2, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('e', 2, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('e', 1, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('d', 1, new Rei(tabuleiro, Cor.BRANCO));

		posicaoNovaPeca('c', 7, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('c', 8, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('d', 7, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('e', 7, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('e', 8, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('d', 8, new Rei(tabuleiro, Cor.PRETO));
	}
}
