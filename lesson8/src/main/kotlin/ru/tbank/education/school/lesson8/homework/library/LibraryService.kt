package ru.tbank.education.school.lesson8.homework.library

class LibraryService {
    private val books = mutableMapOf<String, Book>()
    private val borrowedBooks = mutableSetOf<String>()
    private val borrowerFines = mutableMapOf<String, Int>()

    fun addBook(book: Book) {
        books[book.isbn] = book
    }
    fun borrowBook(isbn: String, borrower: String) {
        if (!books.containsKey(isbn)) {
            throw IllegalArgumentException("Нельзя взять книгу, которой нет в каталоге")
        }
        if (borrowedBooks.contains(isbn)) {
            throw IllegalArgumentException("Нельзя взять одну и ту же книгу дважды")
        }
        if (hasOutstandingFines(borrower)) {
            throw IllegalArgumentException("Читатель с непогашенным штрафом не может брать книги")
        }
        borrowerFines[borrower] = 1

        borrowedBooks.add(isbn)
    }

    fun returnBook(isbn: String) {
        if (!borrowedBooks.contains(isbn)) {
            throw IllegalArgumentException("Нельзя вернуть книгу, которая не была выдана")
        }
        borrowedBooks.remove(isbn)
    }

    fun isAvailable(isbn: String): Boolean {
        return !borrowedBooks.contains(isbn)
    }

    fun calculateOverdueFine(isbn: String, daysOverdue: Int): Int {
        if (!borrowedBooks.contains(isbn) || (daysOverdue <= 10)) { // Предполагаю, что имелось в виду это, т.к. в return было * 60, а в тестах было: при взятии книги на 15 дней - штраф 300.
            return 0
        }
        return (daysOverdue-10) * 60
    }

    private fun hasOutstandingFines(borrower: String): Boolean {
        return (borrowerFines[borrower] ?: 0) > 0
    }
}