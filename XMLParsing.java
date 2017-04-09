import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;

public class XMLParsing {
	
	public final static double TITLE_W = 0.4;
	public final static double AUTHOR_W = 0.3;
	public final static double GENRE_W = 0.2;
	public final static double TEXT_W = 0.1;

	public int rangeSearch(String s) { // return best doc_id
		int k = -1;
		int coef = 0;
		ArrayList<Integer> contained_docs = new ArrayList<Integer>();
		// search text first
		for(int j=0;j<tokenCount;j++)
			if(s.contains(tokens[j].getText()))
				contained_docs.addAll(tokens[j].docs);
		// then search by metadata to find best variant
		for (int i = 0; i < book_count; i++) {
			Book t = books[i];
			int cur_coef = 0;
			if (t.author.contains(s))
				cur_coef += AUTHOR_W;
			if (t.bookTitle.contains(s))
				cur_coef += TITLE_W;
			for (int j = 0; j < t.genres.length; j++) {
				if (t.genres[j].contains(s)) {
					cur_coef += GENRE_W;
					break;
				}
			}
			if(contained_docs.contains(i))
				cur_coef+=TEXT_W;
			if (cur_coef > coef)
				k = i;
		}
		return k;
	}
	
private void indexBook(int doc_id, File file) {
		StreamTokenizer st;
		Pair[] f = new Pair[5000000];
		int pcounter = 0;
		try {
			st = new StreamTokenizer(new BufferedReader(new FileReader(file)));
			st.whitespaceChars('<', '>');
			st.whitespaceChars('/', '/');
			st.whitespaceChars(',', ',');
			st.whitespaceChars('.', '.');
			st.whitespaceChars('"', '"');
			st.whitespaceChars('_', '_');
			boolean lfauthor = true;
			boolean lftitle = true;
			String author = "";
			String title = "";
			ArrayList<String> genres = new ArrayList<String>();
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.sval != null && st.sval.equals("genre")) {
					st.nextToken();
					String cur = "";
					while (st.sval != null && !st.sval.equals("genre")) {
						cur += st.sval + " ";
						st.nextToken();
					}
					genres.add(cur);
				}
				if (lfauthor && st.sval != null && st.sval.equals("first-name")) {
					st.nextToken();
					author += st.sval;
					st.nextToken();
					st.nextToken();
					st.nextToken();
					author += " " + st.sval;
					lfauthor = false;
				}
				if (lftitle && st.sval != null && st.sval.equals("book-title")) {
					st.nextToken();
					while (st.sval != null && !st.sval.equals("book-title")) {
						title += st.sval + " ";
						st.nextToken();
					}
					lftitle = false;
				}
				if (st.sval != null && st.sval.equals("body")) {
					break;
				}

			}
			String[] r = new String[genres.size()];
			for (int i = 0; i < genres.size(); i++)
				r[i] = genres.get(i);
			Book b = new Book(doc_id, title, author, r);
			books[doc_id] = b;
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.sval != null && st.sval.equals("body"))
					break;
				if (st.sval != null) {
					String current_word = clear(st.sval.toLowerCase());
					Pair p = new Pair(current_word, doc_id);
					f[pcounter++] = p;
				}
				// if (isWord(current_word))
				// consideration(current_word,docnum);
				// consideration(st.sval,doc_id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Book indexed" + doc_id);
		writeBlock(f, 1, pcounter);
	}

}