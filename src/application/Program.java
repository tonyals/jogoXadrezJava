package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		ChessMatch partidaXadrez = new ChessMatch();
		List<ChessPiece> capturadas = new ArrayList<>();

		while (!partidaXadrez.getCheckMate()) {
			try {
				UI.limparTela();
				UI.imprimePartida(partidaXadrez, capturadas);
				System.out.println();
				System.out.print("Origem: ");
				ChessPosition origem = UI.lerPosicaoXadrez(sc);
				
				//Imprimir movimentos possíveis:
				boolean[][] movimentosPossiveis = partidaXadrez.movimentosPossiveis(origem);
				UI.limparTela();
				UI.imprimeTabuleiro(partidaXadrez.getPecas(), movimentosPossiveis);

				System.out.println();
				System.out.print("Destino: ");
				ChessPosition destino = UI.lerPosicaoXadrez(sc);

				ChessPiece pecaCapturada = partidaXadrez.movimentaPeca(origem, destino);
				
				if(pecaCapturada != null) {
					capturadas.add(pecaCapturada);
				}
				
				if(partidaXadrez.getPromoted() != null) {
					System.out.print("Entre com a peca para promocao (B/N/R/Q)");
					String type = sc.nextLine();
					partidaXadrez.replacePromotedPiece(type);
				}
			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();

			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
		UI.limparTela();
		UI.imprimePartida(partidaXadrez, capturadas);
	}
}
