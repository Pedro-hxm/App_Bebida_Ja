package br.com.bebidaja.data

enum class FormaPagamento(val descricao: String) {
    PIX("Pix"),
    DINHEIRO("Dinheiro"),
    CARTAO_NA_ENTREGA("Cartão na entrega")
}
