package chess;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

//Essa � a classe principal, nela est�o todas as regras do jogo de xadrez

public class ChessMatch {

	private int turno;
	private Color currentPlayer;
	private Board tabuleiro;
	private ChessPiece enPassantVulnerable;
	private ChessPiece promoted;

	// Por padr�o um boolean come�a com false:
	private boolean check;
	private boolean checkMate;

	private List<Piece> pecasNoTabuleiro = new ArrayList<>();
	private List<Piece> pecasCapturadas = new ArrayList<>();

	// Construtor que define o tamanho do tabuleiro:
	public ChessMatch() {
		tabuleiro = new Board(8, 8);
		turno = 1;
		currentPlayer = Color.WHITE;
		iniciaPartida();
	}

	public int getTurno() {
		return turno;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean getCheck() {
		return check;
	}

	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}
	
	public ChessPiece getPromoted() {
		return promoted;
	}

	// M�todo para retornar uma matriz de pe�as de xadrez correspondente a esta
	// partida (ChessMatch):
	public ChessPiece[][] getPecas() {
		ChessPiece[][] mat = new ChessPiece[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for (int i = 0; i < tabuleiro.getLinhas(); i++) {
			for (int j = 0; j < tabuleiro.getColunas(); j++) {
				mat[i][j] = (ChessPiece) tabuleiro.peca(i, j);
			}
		}
		return mat;
	}

	// M�todo para colorir as movimenta��es poss�veis de cada pe�a
	public boolean[][] movimentosPossiveis(ChessPosition posicaoOrigem) {
		Position posicao = posicaoOrigem.paraPosicao();
		validarPosicaoOrigem(posicao);
		return tabuleiro.peca(posicao).movimentosPossiveis();
	}

	// performChessMove
	public ChessPiece movimentaPeca(ChessPosition posicaoOrigem, ChessPosition posicaoDestino) {
		Position origem = posicaoOrigem.paraPosicao();
		Position destino = posicaoDestino.paraPosicao();
		validarPosicaoOrigem(origem);
		validarPosicaoDestino(origem, destino);
		Piece pecaCapturada = realizaMovimento(origem, destino);

		if (testCheck(currentPlayer)) {
			desfazerMovimento(origem, destino, pecaCapturada);
			throw new ChessException("Voce nao pode fazer um movimento que coloque seu Rei em xeque");
		}

		ChessPiece movedPiece = (ChessPiece) tabuleiro.peca(destino);
		
		promoted = null;
		if(movedPiece instanceof Pawn) {
			if((movedPiece.getCor() == Color.WHITE && destino.getLinha() == 0)
					|| (movedPiece.getCor() == Color.BLACK && destino.getLinha() == 7)) {
				promoted = (ChessPiece)tabuleiro.peca(destino);
				promoted = replacePromotedPiece("Q");
			}
		}
		
		// Se o teste check do oponente for verdadeiro, ent�o check receber� true, :
		// (sen�o) receber� false:
		check = (testCheck(oponente(currentPlayer))) ? true : false;

		if (testCheckMate(oponente(currentPlayer))) {
			checkMate = true;
		} else {
			proximoTurno();
		}

		// En passant
		if (movedPiece instanceof Pawn && (destino.getLinha() == origem.getLinha() - 2)
				|| (destino.getLinha() == origem.getLinha() + 2)) {
			enPassantVulnerable = movedPiece;
		} else {
			enPassantVulnerable = null;
		}

		// Downcasting (Convers�o) de peca capturada j� que ela era do tipo Piece.
		return (ChessPiece) pecaCapturada;
	}
	
	public ChessPiece replacePromotedPiece(String type) {
		if(promoted == null) {
			throw new IllegalStateException("Nao ha peca para ser promovida");
		}
		if(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
			throw new InvalidParameterException("Tipo invalido para promocao");
		}
		
		Position pos = promoted.getPosicaoPeca().paraPosicao();
		Piece p = tabuleiro.removePeca(pos);
		pecasNoTabuleiro.remove(p);
		
		ChessPiece newPiece = newPiece(type, promoted.getCor());
		tabuleiro.colocarPeca(newPiece, pos);
		pecasNoTabuleiro.add(newPiece);
		
		return newPiece;
	}
	
	private ChessPiece newPiece(String type, Color cor) {
		if(type.equals("B")) return new Bishop(tabuleiro, cor);
		if(type.equals("N")) return new Knight(tabuleiro, cor);
		if(type.equals("Q")) return new Queen(tabuleiro, cor);
		return new Rook(tabuleiro, cor);
	}
	
	// makeMovie
	private Piece realizaMovimento(Position origem, Position destino) {
		// Remove a pe�a da posi��o de origem:
		ChessPiece p = (ChessPiece) tabuleiro.removePeca(origem);
		p.incrementaContadorMovimento();
		// Remove a poss�vel pe�a que esteja na posi��o de destino:
		Piece pecaCapturada = tabuleiro.removePeca(destino);

		// Coloca a pe�a p na posi��o de destino:
		tabuleiro.colocarPeca(p, destino);

		if (pecaCapturada != null) {
			pecasNoTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}
		// Castling - roque pequeno
		if (p instanceof King && destino.getColuna() == origem.getColuna() + 2) {
			Position sourceT = new Position(origem.getLinha(), origem.getColuna() + 3);
			Position targetT = new Position(origem.getLinha(), origem.getColuna() + 1);
			ChessPiece rook = (ChessPiece) tabuleiro.removePeca(sourceT);
			tabuleiro.colocarPeca(rook, targetT);
			rook.incrementaContadorMovimento();
		}

		// Castling - roque grande
		if (p instanceof King && destino.getColuna() == origem.getColuna() - 2) {
			Position sourceT = new Position(origem.getLinha(), origem.getColuna() - 4);
			Position targetT = new Position(origem.getLinha(), origem.getColuna() - 1);
			ChessPiece rook = (ChessPiece) tabuleiro.removePeca(sourceT);
			tabuleiro.colocarPeca(rook, targetT);
			rook.incrementaContadorMovimento();
		}

		// En passant
		if (p instanceof Pawn) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == null) {
				Position pawnPosition;
				if (p.getCor() == Color.WHITE) {
					pawnPosition = new Position(destino.getLinha() + 1, destino.getColuna());
				} else {
					pawnPosition = new Position(destino.getLinha() - 1, destino.getColuna());
				}
				pecaCapturada = tabuleiro.removePeca(pawnPosition);
				pecasCapturadas.add(pecaCapturada);
				pecasNoTabuleiro.remove(pecaCapturada);
			}
		}
		return pecaCapturada;
	}

	private void desfazerMovimento(Position origem, Position destino, Piece pecaCapturada) {
		ChessPiece p = (ChessPiece) tabuleiro.removePeca(destino);
		p.decrementaContadorMovimento();
		tabuleiro.colocarPeca(p, origem);

		// Adiciona uma poss�vel pe�a capturada novamente a sua posi��o de origem:
		if (pecaCapturada != null) {
			tabuleiro.colocarPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasNoTabuleiro.add(pecaCapturada);
		}

		// Castling - roque pequeno
		if (p instanceof King && destino.getColuna() == origem.getColuna() + 2) {
			Position sourceT = new Position(origem.getLinha(), origem.getColuna() + 3);
			Position targetT = new Position(origem.getLinha(), origem.getColuna() + 1);
			ChessPiece rook = (ChessPiece) tabuleiro.removePeca(targetT);
			tabuleiro.colocarPeca(rook, sourceT);
			rook.decrementaContadorMovimento();
		}

		// Castling - roque grande
		if (p instanceof King && destino.getColuna() == origem.getColuna() - 2) {
			Position sourceT = new Position(origem.getLinha(), origem.getColuna() - 4);
			Position targetT = new Position(origem.getLinha(), origem.getColuna() - 1);
			ChessPiece rook = (ChessPiece) tabuleiro.removePeca(targetT);
			tabuleiro.colocarPeca(rook, sourceT);
			rook.decrementaContadorMovimento();
		}
		
		// En passant
		if (p instanceof Pawn) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece)tabuleiro.removePeca(destino);
				Position pawnPosition;
				if (p.getCor() == Color.WHITE) {
					pawnPosition = new Position(3, destino.getColuna());
				} else {
					pawnPosition = new Position(4, destino.getColuna());
				}
				tabuleiro.colocarPeca(pawn, pawnPosition);
			}
		}
	}

	private void validarPosicaoOrigem(Position posicao) {
		if (!tabuleiro.posicaoTemPeca(posicao)) {
			throw new ChessException("N�o existe pe�a na posi��o de origem.");
		}
		if (currentPlayer != ((ChessPiece) tabuleiro.peca(posicao)).getCor()) {
			throw new ChessException("Voce nao pode selecionar uma peca adversaria");
		}
		if (!tabuleiro.peca(posicao).existeMovimentoPossivel()) {
			throw new ChessException("N�o h� movimentos poss�veis para esta pe�a.");
		}
	}

	private void validarPosicaoDestino(Position origem, Position destino) {
		if (!tabuleiro.peca(origem).movimentoPossivel(destino)) {
			throw new ChessException("A peca escolhida n�o pode se mover para a posicao destino.");
		}
	}

	private void proximoTurno() {
		turno++;
		// Se o jogador atual � == Color.WHITE ent�o agora vai ser Color.BLACK. Caso
		// contr�rio ":" Color.WHITE.
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private Color oponente(Color cor) {
		return (cor == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private ChessPiece king(Color cor) {
		List<Piece> list = pecasNoTabuleiro.stream().filter(x -> ((ChessPiece) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Piece p : list) {
			// Se a pe�a for uma inst�ncia de Rei(King):
			if (p instanceof King) {
				return (ChessPiece) p;
			}
		}
		// Se ocorrer esta exce��o h� um problema no jogo como um todo pois sempre
		// haver� um rei antes do fim do jogo.
		throw new IllegalStateException("N�o h� nenhum rei da cor " + cor + "no tabuleiro");
	}

	// Teste do rei em xeque:
	private boolean testCheck(Color cor) {
		Position posicaoDoRei = king(cor).getPosicaoPeca().paraPosicao();
		List<Piece> pecasOponente = pecasNoTabuleiro.stream().filter(x -> ((ChessPiece) x).getCor() == oponente(cor))
				.collect(Collectors.toList());
		for (Piece p : pecasOponente) {
			boolean[][] mat = p.movimentosPossiveis();
			if (mat[posicaoDoRei.getLinha()][posicaoDoRei.getColuna()]) {
				return true;
			}
		}
		return false;
	}

	private boolean testCheckMate(Color cor) {

		if (!testCheck(cor)) {
			return false;
		}
		List<Piece> list = pecasNoTabuleiro.stream().filter(x -> ((ChessPiece) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Piece p : list) {
			boolean[][] mat = p.movimentosPossiveis();
			for (int i = 0; i < tabuleiro.getLinhas(); i++) {
				for (int j = 0; j < tabuleiro.getColunas(); j++) {
					if (mat[i][j]) {
						Position origem = ((ChessPiece) p).getPosicaoPeca().paraPosicao();
						Position destino = new Position(i, j);
						Piece pecaCapturada = realizaMovimento(origem, destino);
						boolean testeCheck = testCheck(cor);
						desfazerMovimento(origem, destino, pecaCapturada);
						if (!testeCheck) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private void colocarNovaPeca(char coluna, int linha, ChessPiece peca) {
		tabuleiro.colocarPeca(peca, new ChessPosition(coluna, linha).paraPosicao());
		pecasNoTabuleiro.add(peca);
	}

	private void iniciaPartida() {

		colocarNovaPeca('a', 1, new Rook(tabuleiro, Color.WHITE));
		colocarNovaPeca('b', 1, new Knight(tabuleiro, Color.WHITE));
		colocarNovaPeca('c', 1, new Bishop(tabuleiro, Color.WHITE));
		colocarNovaPeca('d', 1, new Queen(tabuleiro, Color.WHITE));
		colocarNovaPeca('e', 1, new King(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('f', 1, new Bishop(tabuleiro, Color.WHITE));
		colocarNovaPeca('g', 1, new Knight(tabuleiro, Color.WHITE));
		colocarNovaPeca('h', 1, new Rook(tabuleiro, Color.WHITE));
		colocarNovaPeca('a', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('b', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('c', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('d', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('e', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('f', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('g', 2, new Pawn(tabuleiro, Color.WHITE, this));
		colocarNovaPeca('h', 2, new Pawn(tabuleiro, Color.WHITE, this));

		colocarNovaPeca('a', 8, new Rook(tabuleiro, Color.BLACK));
		colocarNovaPeca('b', 8, new Knight(tabuleiro, Color.BLACK));
		colocarNovaPeca('c', 8, new Bishop(tabuleiro, Color.BLACK));
		colocarNovaPeca('d', 8, new Queen(tabuleiro, Color.BLACK));
		colocarNovaPeca('e', 8, new King(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('f', 8, new Bishop(tabuleiro, Color.BLACK));
		colocarNovaPeca('g', 8, new Knight(tabuleiro, Color.BLACK));
		colocarNovaPeca('h', 8, new Rook(tabuleiro, Color.BLACK));
		colocarNovaPeca('a', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('b', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('c', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('d', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('e', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('f', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('g', 7, new Pawn(tabuleiro, Color.BLACK, this));
		colocarNovaPeca('h', 7, new Pawn(tabuleiro, Color.BLACK, this));
	}
}
