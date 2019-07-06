package chess;

import boardgame.Board;
import chess.pieces.King;
import chess.pieces.Rook;

//Essa � a classe principal, nela est�o todas as regras do jogo de xadrez

public class ChessMatch {
	
	private Board tabuleiro;
	
	//Construtor que define o tamanho do tabuleiro:
	public ChessMatch() {
		tabuleiro = new Board(8, 8);
		iniciaPartida();
	}
	
	//M�todo para retornar uma matriz de pe�as de xadrez correspondente a esta partida (ChessMatch):
	public ChessPiece[][] getPecas() {
		ChessPiece[][] mat = new ChessPiece[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for (int i = 0; i < tabuleiro.getLinhas(); i++) {
			for (int j = 0; j < tabuleiro.getColunas(); j++) {
				mat[i][j] = (ChessPiece) tabuleiro.peca(i, j);
			}
		}
		return mat;
	}
	
	private void colocarNovaPeca(char coluna, int linha, ChessPiece peca) {
		tabuleiro.colocarPeca(peca, new ChessPosition(coluna, linha).paraPosicao());
	}
	
	private void iniciaPartida() {
		colocarNovaPeca('b', 6, new Rook(tabuleiro, Color.WHITE));
		colocarNovaPeca('e', 8, new King(tabuleiro, Color.BLACK));
		colocarNovaPeca('e', 1, new King(tabuleiro, Color.WHITE));
	}
}
