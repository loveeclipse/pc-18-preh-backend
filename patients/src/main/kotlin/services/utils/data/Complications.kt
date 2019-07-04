package services.utils.data

enum class Complications(private val complicationName: String) {
    CARDIOCIRCULATORY_SHOCK("shock cardiocircolatorio"),
    STATE_OF_CONSCIOUSNESS_DETERIORATION("deterioramento stato di coscenza"),
    ANISPCORIA_MYDRIASIS("anisocoria / midriasi"),
    RESPIRATORY_DEFICIENCY("insufficienza respiratoria"),
    LANDING_ONGOING("atterraggio in itinere per manovra terapeutica"),
    DEATH_ONGOING("decesso in itinere"),
    DEATH_IN_ER("decesso all'arrivo in ps")
}