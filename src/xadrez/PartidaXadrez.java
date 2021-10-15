package xadrez;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import camadaTabuleiro.Peca;
import camadaTabuleiro.Posicao;
import camadaTabuleiro.Tabuleiro;
import xadrez.pecas.Bispo;
import xadrez.pecas.Cavalo;
import xadrez.pecas.Peao;
import xadrez.pecas.Rainha;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorAtual;
	private Tabuleiro tabuleiro;
	private boolean xeque;
	private boolean xequeMate;
	private PecaXadrez enPassantVulneravel;
	private PecaXadrez promocao;

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

	public boolean getXequeMate() {
		return xequeMate;
	}

	public PecaXadrez getEnPassantVulneravel() {
		return enPassantVulneravel;
	}
	
	public PecaXadrez getPromocao() {
		return promocao;
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
		
		PecaXadrez pecaMovida = (PecaXadrez) tabuleiro.peca(destino);

		// #movimentoEspecial promocao
		promocao = null;
		if (pecaMovida instanceof Peao) {
			if ((pecaMovida.getCor() == Cor.BRANCO && destino.getLinha() == 0) || (pecaMovida.getCor() == Cor.PRETO && destino.getLinha() == 7)) {
				promocao = (PecaXadrez) tabuleiro.peca(destino);
				promocao = substituirPecaPromovida("Q");
			}
		}
		
		xeque = (testeXeque(oponente(jogadorAtual))) ? true : false;

		if (testeXequeMate(oponente(jogadorAtual))) {
			xequeMate = true;
		} else {
			proximoTurno();
		}
		
		// #movimentoEspecial en passant
		if (pecaMovida instanceof Peao && (destino.getLinha() == origem.getLinha() - 2 || destino.getLinha() == origem.getLinha() + 2)) {
			enPassantVulneravel = pecaMovida;
		} else {
			enPassantVulneravel = null;
		}

		return (PecaXadrez) pecaCapturada;
	}

	public PecaXadrez substituirPecaPromovida(String tipo) {
	
		if (promocao == null) {
			throw new IllegalStateException("Nao ha peca para ser promovida");
		}
		if (!tipo.equals("B") && !tipo.equals("C") && !tipo.equals("T") && !tipo.equals("Q")) {
			return promocao;
		}
		
		Posicao pos = promocao.getPosicaoXadrez().toPosicao();
		Peca p = tabuleiro.removerPeca(pos);
		pecasNoTabuleiro.remove(p);
		
		PecaXadrez novaPeca = novaPeca(tipo, promocao.getCor());
		tabuleiro.posicaoPeca(novaPeca, pos);
		pecasNoTabuleiro.add(novaPeca);
		
		return novaPeca;
	}
	
	private PecaXadrez novaPeca(String tipo, Cor cor) {
		if (tipo.equals("B")) return new Bispo(tabuleiro, cor);
		if (tipo.equals("C")) return new Cavalo(tabuleiro, cor);
		if (tipo.equals("Q")) return new Rainha(tabuleiro, cor);
		return new Torre(tabuleiro, cor);
	}

	private Peca moverPeca(Posicao origem, Posicao destino) {
		PecaXadrez p = (PecaXadrez) tabuleiro.removerPeca(origem);
		p.incrementarMovimentos();
		Peca pecaCapturada = tabuleiro.removerPeca(destino);
		tabuleiro.posicaoPeca(p, destino);

		if (pecaCapturada != null) {
			pecasNoTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}

		// #movimentoEspecial Roque ao lado do rei
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(origemT);
			tabuleiro.posicaoPeca(torre, destinoT);
			torre.incrementarMovimentos();
		}

		// #movimentoEspecial Roque ao lado da rainha
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() - 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() - 4);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(origemT);
			tabuleiro.posicaoPeca(torre, destinoT);
			torre.incrementarMovimentos();
		}
		
		// #movimentoEspecial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == null) {
				Posicao posicaoPeao;
				if (p.getCor() == Cor.BRANCO) {
					posicaoPeao = new Posicao(destino.getLinha() + 1, destino.getColuna());
				} else {
					posicaoPeao = new Posicao(destino.getLinha() - 1, destino.getColuna());
				}
				pecaCapturada = tabuleiro.removerPeca(posicaoPeao);
				pecasCapturadas.add(pecaCapturada);
				pecasNoTabuleiro.remove(pecaCapturada);
			}
		}

		return pecaCapturada;
	}

	private void desfazerMovimento(Posicao origem, Posicao destino, Peca pecaCapturada) {
		PecaXadrez p = (PecaXadrez) tabuleiro.removerPeca(destino);
		p.decrementarMovimentos();
		tabuleiro.posicaoPeca(p, origem);

		if (pecaCapturada != null) {
			tabuleiro.posicaoPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasNoTabuleiro.add(pecaCapturada);
		}

		// #movimentoEspecial Roque ao lado do rei
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(destinoT);
			tabuleiro.posicaoPeca(torre, origemT);
			torre.decrementarMovimentos();
		}

		// #movimentoEspecial Roque ao lado da rainha
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() - 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() - 4);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(destinoT);
			tabuleiro.posicaoPeca(torre, origemT);
			torre.decrementarMovimentos();
		}
		
		// #movimentoEspecial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == enPassantVulneravel) {
				PecaXadrez peao = (PecaXadrez) tabuleiro.removerPeca(destino);
				Posicao posicaoPeao;
				if (p.getCor() == Cor.BRANCO) {
					posicaoPeao = new Posicao(3, destino.getColuna());
				} else {
					posicaoPeao = new Posicao(4, destino.getColuna());
				}
				tabuleiro.posicaoPeca(peao, posicaoPeao);
			}
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
		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Peca p : lista) {
			if (p instanceof Rei) {
				return (PecaXadrez) p;
			}
		}
		throw new IllegalStateException("Nao existe o rei da cor " + cor + " no tabuleiro");
	}

	private boolean testeXeque(Cor cor) {
		Posicao posicaoRei = rei(cor).getPosicaoXadrez().toPosicao();
		List<Peca> pecasOponentes = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == oponente(cor))
				.collect(Collectors.toList());
		for (Peca p : pecasOponentes) {
			boolean[][] mat = p.possiveisMovimentos();
			if (mat[posicaoRei.getLinha()][posicaoRei.getColuna()]) {
				return true;
			}
		}

		return false;
	}

	private boolean testeXequeMate(Cor cor) {

		if (!testeXeque(cor)) {
			return false;
		}
		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Peca p : lista) {
			boolean[][] mat = p.possiveisMovimentos();
			for (int i = 0; i < tabuleiro.getLinhas(); i++) {
				for (int j = 0; j < tabuleiro.getColunas(); j++) {
					if (mat[i][j]) {
						Posicao origem = ((PecaXadrez) p).getPosicaoXadrez().toPosicao();
						Posicao destino = new Posicao(i, j);
						Peca pecaCapturada = moverPeca(origem, destino);
						boolean testeXeque = testeXeque(cor);
						desfazerMovimento(origem, destino, pecaCapturada);
						if (!testeXeque) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	private void posicaoNovaPeca(char coluna, int linha, PecaXadrez pecaXadrez) {
		tabuleiro.posicaoPeca(pecaXadrez, new PosicaoXadrez(coluna, linha).toPosicao());
		pecasNoTabuleiro.add(pecaXadrez);
	}

	private void setupInicial() {
		posicaoNovaPeca('a', 1, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('b', 1, new Cavalo(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('c', 1, new Bispo(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('d', 1, new Rainha(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('e', 1, new Rei(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('f', 1, new Bispo(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('g', 1, new Cavalo(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('h', 1, new Torre(tabuleiro, Cor.BRANCO));
		posicaoNovaPeca('a', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('b', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('c', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('d', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('e', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('f', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('g', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		posicaoNovaPeca('h', 2, new Peao(tabuleiro, Cor.BRANCO, this));

		posicaoNovaPeca('a', 8, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('b', 8, new Cavalo(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('c', 8, new Bispo(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('d', 8, new Rainha(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('e', 8, new Rei(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('f', 8, new Bispo(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('g', 8, new Cavalo(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('h', 8, new Torre(tabuleiro, Cor.PRETO));
		posicaoNovaPeca('a', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('b', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('c', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('d', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('e', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('f', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('g', 7, new Peao(tabuleiro, Cor.PRETO, this));
		posicaoNovaPeca('h', 7, new Peao(tabuleiro, Cor.PRETO, this));

	}
}
