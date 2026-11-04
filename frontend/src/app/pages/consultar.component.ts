import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, ConsultaChaveResponse, lerErro } from '../api.service';
import { IconComponent } from '../shared/icon.component';
import { RiscoCardComponent, TopbarComponent } from '../shared/ui';

@Component({
  selector: 'app-consultar',
  standalone: true,
  imports: [FormsModule, RouterLink, IconComponent, TopbarComponent, RiscoCardComponent],
  template: `
    @if (!resultado()) {
      <app-topbar titulo="Consultar chave" />
      <main class="conteudo">
        <h2>Para quem vai o Pix?</h2>
        <p class="sub">Digite a chave que você recebeu. Mostramos se outras pessoas já denunciaram.</p>

        <form (ngSubmit)="consultar()">
          <label for="chave">Chave Pix</label>
          <div class="campo" [class.erro]="erroCampo()">
            <app-icon name="search" [size]="18" color="#5B667A" />
            <input id="chave" name="chave" [(ngModel)]="chave" maxlength="140"
                   placeholder="CPF, celular, e-mail ou chave aleatória" autocomplete="off" />
          </div>
          @if (erroCampo()) { <p class="msg-erro">{{ erroCampo() }}</p> }
          <p class="nota"><app-icon name="lock" [size]="14" /> A chave consultada não fica salva.</p>

          <button class="btn btn-primario" type="submit" [disabled]="!chave.trim() || carregando()">
            <app-icon name="search" [size]="18" /> {{ carregando() ? 'Consultando...' : 'Consultar' }}
          </button>
        </form>
        @if (erroGeral()) { <p class="msg-erro">{{ erroGeral() }}</p> }

        <aside class="info">
          <p class="info-titulo"><app-icon name="info" [size]="18" color="#1E5BD8" /> Como funciona?</p>
          <p>0 denúncias = risco baixo · 1 a 2 = médio · 3 ou mais = alto.</p>
        </aside>
      </main>
    } @else {
      <app-topbar titulo="Resultado" voltarPara="/consultar" />
      <main class="conteudo">
        <p class="sub">Chave consultada: <strong>{{ chave }}</strong></p>
        <app-risco-card [risco]="resultado()!.risco">
          {{ resultado()!.totalDenuncias }} denúncia(s) registrada(s) para esta chave
        </app-risco-card>

        <section class="card">
          <h3>O que fazer agora</h3>
          <p>{{ resultado()!.mensagem }}</p>
        </section>

        @if (resultado()!.risco !== 'BAIXO') {
          <a class="btn btn-perigo" routerLink="/denunciar" [queryParams]="{ chave: chave }">
            <app-icon name="flag" [size]="18" /> Denunciar esta chave
          </a>
        }
        <button class="btn btn-secundario" (click)="novaConsulta()"><app-icon name="redo" [size]="18" /> Consultar outra chave</button>
        <a class="btn btn-secundario" routerLink="/"><app-icon name="home" [size]="18" /> Voltar ao início</a>
      </main>
    }
  `,
})
export class ConsultarComponent {
  private api = inject(ApiService);
  chave = '';
  carregando = signal(false);
  resultado = signal<ConsultaChaveResponse | null>(null);
  erroGeral = signal('');
  erroCampo = signal('');

  consultar() {
    this.erroGeral.set(''); this.erroCampo.set('');
    this.carregando.set(true);
    this.api.consultarChave(this.chave.trim()).subscribe({
      next: (r) => { this.resultado.set(r); window.scrollTo(0, 0); this.carregando.set(false); },
      error: (e: HttpErrorResponse) => {
        const { geral, campos } = lerErro(e);
        this.erroCampo.set(campos['chave'] ?? '');
        this.erroGeral.set(campos['chave'] ? '' : geral);
        this.carregando.set(false);
      },
    });
  }

  novaConsulta() { this.resultado.set(null); this.chave = ''; }
}
