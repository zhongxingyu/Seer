 package com.burningman.tictactoe;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Created by Anton on 24.05.13.
  */
 public class Core implements Parcelable {
     private Field field;
 
     public Core() {
         field = new Field();
     }
 
     public Core(Parcel in) {
         readFromParcel(in);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         int[] fieldInt = new int[3];
         int counter = 0;
         for (int i = 0; i < 3; i++)
             for (int j = 0; j < 3; j++)
                 fieldInt[counter++] = (get_Field(j, i) == Player.none) ? 0 : (get_Field(j, i) == Player.circle) ? -1 : 1;
         for (int i = 0; i < 3; i++)
             dest.writeIntArray(fieldInt);
     }
 
     private void readFromParcel(Parcel in) {
         int counter = 0;
         int[] tmp = new int[9];
         in.readIntArray(tmp);
         for (int i = 0; i < 2; i++) {
             Player player = (tmp[counter]) == 0 ? Player.none : ((tmp[counter++] < 0) ? Player.circle : Player.cross);
             set_Field(0, 3 * i, player);
         }
     }
 
     public static final Parcelable.Creator<Core> CREATOR = new Parcelable.Creator<Core>() {
         public Core createFromParcel(Parcel in) {
             return new Core(in);
         }
 
         public Core[] newArray(int size) {
             return new Core[size];
         }
     };
 
     public Player get_Field(int n, int m) {
         return field.get_field(n, m);
     }
 
     public Player get_Field(int n) {
         if (n < 4)
             return field.get_field(0, n - 1);
         if (n < 7)
             return field.get_field(1, n - 4);
        if (n < 8)
            return field.get_field(2, n - 7);
        return null;

     }
 
     public void set_Field(int n, int m, Player player) {
         field.set_field(n, m, player);
     }
 
     public class Field {
         private Player[][] field;
 
         public Field() {
             field = new Player[3][3];
             for (int i = 0; i < 3; i++)
                 for (int j = 0; j < 3; j++)
                     field[i][j] = Player.none;
         }
 
         public void set_field(int n, int m, Player player) {
             field[n][m] = player;
         }
 
         public Player get_field(int n, int m) {
             return field[n][m];
         }
 
 
     }
 
     public Player search_won() {
         for (int i = 0; i < 3; i++)
             if (field.get_field(0, i) == field.get_field(1, i) && field.get_field(0, i) == field.get_field(2, i) && field.get_field(1, i) != Player.none)
                 return field.get_field(0, i);
         for (int i = 0; i < 3; i++)
             if (field.get_field(i, 0) == field.get_field(i, 1) && field.get_field(i, 0) == field.get_field(i, 2) & field.get_field(i, 1) != Player.none)
                 return field.get_field(i, 0);
         if (field.get_field(0, 0) == field.get_field(1, 1) && field.get_field(0, 0) == field.get_field(2, 2) & field.get_field(1, 1) != Player.none)
             return field.get_field(0, 0);
         if (field.get_field(2, 0) == field.get_field(1, 1) && field.get_field(2, 0) == field.get_field(0, 2) & field.get_field(1, 1) != Player.none)
             return field.get_field(2, 0);
         return Player.none;
     }
 
     public boolean check_draw() {
         for (int i = 0; i < 3; i++)
             for (int j = 0; j < 3; j++)
                 if (get_Field(i, j) == Player.none)
                     return false;
         return true;
     }
 
 
 }
