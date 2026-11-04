import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, AutodiagnosticoResponse, Pergunta, lerErro } from '../api.service';
import { IconComponent } from '../shared/icon.component';
import { RiscoCardComponent, TopbarComponent } from '../shared/ui';

@Component({
  selector: 'app-autodiagnostico',
  standalone: true,
  imports: [RouterLink, IconComponent, TopbarComponent, RiscoCardComponent],
  template: `
    @if (!resultado()) {
      <app-topbar titulo="Autodiagnóstico" />
      <main class="conteudo">
        <div class="progresso-topo">
          <strong>Sobre o pedido de Pix</strong>
          <span>{{ respondidas() }} de {{ perguntas().length || 5 }}</span>
        </div>
        <div class="progresso"><div [style.width.%]="perguntas().length ? respondidas() / perguntas().length * 100 : 0"></div></div>

        @for (p of perguntas(); track p.id; let i = $index) {
          <div class="pergunta">
            <p>{{ p.texto }}</p>
            <div class="segmento" role="group" [attr.aria-label]="p.texto">
              <button type="button" [class.sim]="respostas()[i] === true" (click)="responder(i, true)">Sim</button>
              <button type="button" [class.nao]="respostas()[i] === false" (click)="responder(i, false)">Não</button>
            </div>
          </div>
        }
        @if (erroGeral()) { <p class="msg-erro">{{ erroGeral() }}</p> }

        <button class="btn btn-primario" [disabled]="!completo() || carregando()" (click)="enviar()">
          {{ carregando() ? 'Calculando...' : 'Ver resultado' }}
        </button>
      </main>
    } @else {
      <app-topbar titulo="Seu resultado" voltarPara="/autodiagnostico" />
      <main class="conteudo">
        <app-risco-card [risco]="resultado()!.risco">Pontuação {{ resultado()!.pontuacao }} de 100</app-risco-card>
        <div class="barra"><div [class]="'barra-' + resultado()!.risco.toLowerCase()" [style.width.%]="resultado()!.pontuacao"></div></div>
        <div class="barra-legenda"><span>Baixo</span><span>Médio</span><span>Alto</span></div>

        <section class="card">
          <h3>Recomendações</h3>
          <ul class="lista-check">
            @for (r of resultado()!.recomendacoes; track r) {
              <li><app-icon name="check" [size]="18" color="#1E8E5A" /> {{ r }}</li>
            }
          </ul>
        </section>

        @if (resultado()!.risco !== 'BAIXO') {
          <a class="btn btn-perigo" routerLink="/denunciar"><app-icon name="flag" [size]="18" /> Denunciar chave</a>
        }
        <button class="btn btn-secundario" (click)="refazer()"><app-icon name="redo" [size]="18" /> Refazer</button>
        <a class="btn btn-secundario" routerLink="/"><app-icon name="home" [size]="18" /> Voltar ao início</a>
      </main>
    }
  `,
})
export class AutodiagnosticoComponent implements OnInit {
  private api = inject(ApiService);
  perguntas = signal<Pergunta[]>([]);
  respostas = signal<(boolean | null)[]>([]);
  respondidas = computed(() => this.respostas().filter((r) => r !== null).length);
  completo = computed(() => this.perguntas().length > 0 && this.respondidas() === this.perguntas().length);
  carregando = signal(false);
  resultado = signal<AutodiagnosticoResponse | null>(null);
  erroGeral = signal('');

  ngOnInit() {
    this.api.perguntas().subscribe({
      next: (ps) => { this.perguntas.set(ps); this.respostas.set(ps.map(() => null)); },
      error: (e: HttpErrorResponse) => this.erroGeral.set(lerErro(e).geral),
    });
  }

  responder(i: number, valor: boolean) {
    const r = [...this.respostas()]; r[i] = valor; this.respostas.set(r);
  }

  enviar() {
    this.carregando.set(true); this.erroGeral.set('');
    this.api.autodiagnostico(this.respostas() as boolean[]).subscribe({
      next: (r) => { this.resultado.set(r); window.scrollTo(0, 0); this.carregando.set(false); },
      error: (e: HttpErrorResponse) => { this.erroGeral.set(lerErro(e).geral); this.carregando.set(false); },
    });
  }

  refazer() { this.resultado.set(null); this.respostas.set(this.perguntas().map(() => null)); }
}
