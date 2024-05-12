/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author BAP1
 */
public class TestBruteForceAttack {

    public static void main(String[] args) {
        String pass = "zdemox";
        char[] pool = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        for (int i = 0; i < 1; i++) {
            CMatrix.getInstance().bruteForceAttack(pool, pass, false, 1_000_000_000);
        }
        System.out.println("merhaba");
        System.exit(0);
    }
}
