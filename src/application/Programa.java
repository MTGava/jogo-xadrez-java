package application;

import xadrez.PartidaXadrez;

public class Programa {

	public static void main(String[] args) {
		
		PartidaXadrez partidaXadrez = new PartidaXadrez();
		UI.printTabuleiro(partidaXadrez.getPecas());
	}

}
