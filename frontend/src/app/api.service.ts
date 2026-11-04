import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

/** Contratos espelhados de br.com.pixseguro.web.Dtos (backend Spring Boot). */
export type Risco = 'BAIXO' | 'MEDIO' | 'ALTO';

export type ProximoPasso =
  | 'CONSULTAR_CHAVE'
  | 'NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL'
  | 'DENUNCIAR_CHAVE'
  | 'SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME'
  | 'PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO'
  | 'ALERTA_PARA_PROXIMOS_USUARIOS';

export interface ConsultaChaveResponse {
  risco: Risco;
  totalDenuncias: number;
  ultimaDenunciaEm: string | null;
  mensagem: string;
  proximoPasso: ProximoPasso;
}

export interface AnaliseMensagemResponse {
  iaDisponivel: boolean;
  motor: string;
  risco: Risco;
  pontuacao: number | null;
  sinais: string[];
  dica: string;
  proximoPasso: ProximoPasso;
  aviso: string | null;
}

export interface Pergunta {
  id: number;
  texto: string;
}

export interface AutodiagnosticoResponse {
  pontuacao: number;
  risco: Risco;
  recomendacoes: string[];
  proximoPasso: ProximoPasso;
}

export interface DenunciaRequest {
  chave: string;
  descricao: string | null;
  jaFezPix: boolean;
}

export interface DenunciaResponse {
  totalDenunciasChave: number;
  risco: Risco;
  alertaPublicado: boolean;
  proximoPasso: ProximoPasso;
  orientacoes: string[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = '/api';

  consultarChave(chave: string): Observable<ConsultaChaveResponse> {
    return this.http.post<ConsultaChaveResponse>(`${this.base}/chaves/consulta`, { chave });
  }

  analisarMensagem(texto: string): Observable<AnaliseMensagemResponse> {
    return this.http.post<AnaliseMensagemResponse>(`${this.base}/mensagens/analise`, { texto });
  }

  perguntas(): Observable<Pergunta[]> {
    return this.http.get<Pergunta[]>(`${this.base}/autodiagnostico/perguntas`);
  }

  autodiagnostico(respostas: boolean[]): Observable<AutodiagnosticoResponse> {
    return this.http.post<AutodiagnosticoResponse>(`${this.base}/autodiagnostico`, { respostas });
  }

  denunciar(req: DenunciaRequest): Observable<DenunciaResponse> {
    return this.http.post<DenunciaResponse>(`${this.base}/denuncias`, req);
  }
}

/** Converte o ProblemDetail do backend em mensagem geral + erros por campo. */
export function lerErro(err: HttpErrorResponse): { geral: string; campos: Record<string, string> } {
  if (err.status === 0) {
    return { geral: 'Não foi possível falar com o servidor. Confira se o backend está rodando na porta 8080.', campos: {} };
  }
  const corpo = err.error ?? {};
  return {
    geral: corpo.detail || 'Algo deu errado. Tente de novo.',
    campos: corpo.campos ?? {},
  };
}
