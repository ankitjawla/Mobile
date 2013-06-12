package edu.stevens.cs522;

public class Book {
	private int id;
	private String title;
	private String author;
	private String isbn;
	private String price;

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}

	public String getIsbn() {
		return isbn;
	}

	public String getPrice() {
		return price;
	}

	public Book(int _id, String _title, String _author, String _isbn, String _price) {
		id = _id;
		title = _title;
		author = _author;
		isbn = _isbn;
		price = _price;
	}

	@Override
	public String toString() {
		return title + ": " + price;
	}

}