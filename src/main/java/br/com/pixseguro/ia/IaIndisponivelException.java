package br.com.pixseguro.ia;

public class IaIndisponivelException extends RuntimeException {
    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
