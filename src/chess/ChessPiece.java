package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece{
	
	private Color cor;
	private int moveCount;

	public ChessPiece(Board tabuleiro, Color cor) {
		super(tabuleiro);
		this.cor = cor;
	}

	public Color getCor() {
		return cor;
	}
	
	public int getMoveCount() {
		return moveCount;
	}
	
	public void incrementaContadorMovimento() {
		moveCount++;
	}
	
	public void decrementaContadorMovimento() {
		moveCount--;
	}
	
	public ChessPosition getPosicaoPeca() {
		return ChessPosition.dePosicao(posicao);
	}
	
	protected boolean pecaDoOponente(Position posicao) {
		ChessPiece p = (ChessPiece)getTabuleiro().peca(posicao);
		
		//Retorna true ou false para:
		return p != null && p.getCor() != cor;
		// p != null, ou seja, se a casa est� livre ela � null && p.getCor testa se a cor da pe�a "p" � diferente 
		//da cor da pe�a na posi��o informad.
	}
}
