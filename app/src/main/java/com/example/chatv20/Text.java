package com.example.chatv20;

public class Text {
    private String t;
    private int kol;
    public Text(String s, int Q) {
        t = s;
        kol = Q;
    }
    public String Text() {
        int j = 0;
        String[] s = t.split(" ");
        if(s.length == 1){
            if (s[0].length() > kol) {
                StringBuilder h = new StringBuilder(s[0]);
                for (int k = 1; k < s[0].length() / kol + 1; k++) {
                    h.insert(kol * k + 2 * (k - 1), "\n\r");
                }
                h.append(" ");
                s[0] = h.toString();
            }
        }
        else {
            for (int i = 0; i < s.length; i++) {
                if (s[i].length() > kol) {
                    StringBuilder h = new StringBuilder(s[i]);
                    for (int k = 1; k < s[i].length() / kol + 1; k++) {
                        h.insert(kol * k + 2 * (k - 1) - j, "\n");
                        j = 0;
                    }
                    j = s[i].length() - s[i].length() / kol * kol;

                    if (i + 1 != s.length && j + s[i + 1].length() > kol) {
                        h.append("\n");
                        j = 0;
                    }
                    else {
                        h.append(" ");
                    }
                    s[i] = h.toString();
                } else {
                    j += s[i].length() + 1;
                    StringBuilder g = new StringBuilder(s[i]);
                    g.append(" ");
                    if (i+1 == s.length){

                    }
                    else if(j + s[i + 1].length() > kol && s[i + 1].length() <= kol) {
                        g.append("\n");
                        j = 0;
                    }
                    s[i] = g.toString();
                }
            }
        }
        StringBuilder f = new StringBuilder();
        for (String value : s) {
            StringBuilder g = new StringBuilder(value);
            f.append(g);
        }
        t = f.toString();
        return t;
    }
}
