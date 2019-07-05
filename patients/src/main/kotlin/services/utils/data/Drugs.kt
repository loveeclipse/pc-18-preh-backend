package services.utils.data

enum class Drugs(private val drugName: String) {
    CRYSTALLOID("cristalloidi"),
    MANNITOL("mannitolo"),
    HYPERTONIC_SOLUTION("soluzione ipertonica NaCl 3%"),
    CONCENTRATED_RED_BLOOD("emazie concentrate"),
    FIBRINOGEN("fibrinogeno"),
    SUCCINYLCHOLINE("succinilcolina"),
    MADAZOLAM("midazolam"),
    FENTANYL("fentanil"),
    KETAMINE("ketamina"),
    CURARE("curaro"),
    TRENEXAMIC_ACID("acidotranexamico");

    override fun toString() = drugName
}