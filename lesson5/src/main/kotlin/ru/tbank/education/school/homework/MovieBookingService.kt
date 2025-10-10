package ru.tbank.education.school.homework

/**
 * Исключение, которое выбрасывается при попытке забронировать занятое место
 */
class SeatAlreadyBookedException(message: String) : Exception(message)

/**
 * Исключение, которое выбрасывается при попытке забронировать место при отсутствии свободных мест
 */
class NoAvailableSeatException(message: String) : Exception(message)

data class BookedSeat(
    val movieId: String, // идентификатор фильма
    val seat: Int // номер места
)

class MovieBookingService(
    private val maxQuantityOfSeats: Int // Максимальное кол-во мест
) {
    init {
        if (maxQuantityOfSeats <= 0) {
            throw IllegalArgumentException("Максимальное кол-во мест отрицательное или равно нулю")
        }
    }
    private val seats: MutableMap<String, MutableMap<Int, Boolean>> = mutableMapOf()
    /**
     * Бронирует указанное место для фильма.
     *
     * @param movieId идентификатор фильма
     * @param seat номер места
     * @throws IllegalArgumentException если номер места вне допустимого диапазона
     * @throws NoAvailableSeatException если нет больше свободных мест
     * @throws SeatAlreadyBookedException если место уже забронировано
     */
    fun bookSeat(movieId: String, seat: Int) {
        if (!seats.containsKey(movieId)) {
            seats[movieId] = (1..maxQuantityOfSeats).associateWith { true } as MutableMap<Int, Boolean> // true - свободное место
        }
        if (seat !in 1..maxQuantityOfSeats) {
            throw IllegalArgumentException("Номер места вне допустимого диапазона")
        }
        else seats[movieId]?.containsValue(true)?.let {
            if (!it) {
                throw NoAvailableSeatException("Нет больше свободных мест")
            }
            else if (isSeatBooked(movieId, seat)) {
                throw SeatAlreadyBookedException("Место уже забронировано")
            }
        }

        seats[movieId]?.put(seat, false)
    }

    /**
     * Отменяет бронь указанного места.
     *
     * @param movieId идентификатор фильма
     * @param seat номер места
     * @throws NoSuchElementException если место не было забронировано
     */
    fun cancelBooking(movieId: String, seat: Int) {
        if (!isSeatBooked(movieId, seat)) {
            throw NoSuchElementException("Место не было забронировано")
        }
        seats[movieId]?.set(seat, true)
    }

    /**
     * Проверяет, забронировано ли место
     *
     * @return true если место занято, false иначе
     */
    fun isSeatBooked(movieId: String, seat: Int): Boolean {
        return seats[movieId]?.get(seat) == false
    }
}


fun main() {
    val service = MovieBookingService(1000)
    println(service.isSeatBooked("movie1", 1))
    service.bookSeat("movie1", 1)
    println(service.isSeatBooked("movie1", 1))
}