package br.com.bebidaja.data

data class EnderecoEntrega(
    val rua: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val complemento: String = ""
) {
    val estaValido: Boolean
        get() = rua.isNotBlank() &&
            numero.isNotBlank() &&
            bairro.isNotBlank() &&
            cidade.isNotBlank() &&
            cep.filter(Char::isDigit).length == 8
}
