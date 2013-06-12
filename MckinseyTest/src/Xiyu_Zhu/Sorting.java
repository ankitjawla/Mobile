package Xiyu_Zhu;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Scanner;

public class Sorting {
	public static void main(String[] args) throws IOException{
		String status = "Yes";
		int i = 0;
		int[][] mk;
		mk = new int[10000][2];
		while(status.equals("Yes"))
		{
			System.out.print("Please input a number:");
			Scanner str=new Scanner(System.in);
			String s=str.next();
			int input = Integer.parseInt(s);
			int diff = 0;
			if(i==0)
			{
				mk[i][0] = input;
				mk[i][1] = 1;
				i++;
			}
			else{
				for(int j = 0; j < i; j++ ){
					if(input == mk[j][0]){
						mk[j][1] = mk[j][1]+1;
					}
					else{
						diff++;
					}
				}
					
				if(diff==i){
					mk[i][0] = input;
					mk[i][1] = 1;
					i++;	
				}
			}
			System.out.println("Continue?Y/N");
			s = str.next();
			if(s.equals("y")||s.equals("Y")){
				status = "Yes";
			}
			else if(s.equals("n")||s.equals("N")){
				status = "No";
			}
		}
		Sort(i, mk);
	}
	
	public static void Sort(int i, int[][] mk){
		int temp1;
		int temp2;
		for(int p = 0; p < i; p++){
			for(int q = 0; q < i; q++){
				if(mk[p][1] > mk[q][1]){
					temp1 = mk[p][1];
					temp2 = mk[p][0];
					mk[p][1] = mk[q][1];
					mk[p][0] = mk[q][0];
					mk[q][1] = temp1;
					mk[q][0] = temp2;
				}
			}
		}
		for(int k=0;k<i;k++){
			if(mk[k][1]>1){
				System.out.print(mk[k][0]+" ");
			}	
		}
	}
}
