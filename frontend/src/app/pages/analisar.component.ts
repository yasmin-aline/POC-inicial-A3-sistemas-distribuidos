import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AnaliseMensagemResponse, ApiService, lerErro } from '../api.service';
import { IconComponent } from '../shared/icon.component';
import { RiscoCardComponent, TopbarComponent } from '../shared/ui';

@Component({
  selector: 'app-analisar',
  standalone: true,
  imports: [FormsModule, RouterLink, IconComponent, TopbarComponent, RiscoCardComponent],
  template: `
    @if (!resultado()) {
      <app-topbar titulo="Analisar mensagem" />
      <main class="conteudo">
        <span class="selo-ia"><app-icon name="sparkles" [size]="16" /> Análise com IA</span>
        <h2>Cole a mensagem que você recebeu</h2>

        <form (ngSubmit)="analisar()">
          <label for="texto">Mensagem</label>
          <textarea id="texto" name="texto" [(ngModel)]="texto" maxlength="2000" rows="6"
                    [class.erro]="erroCampo()"
                    placeholder="Ex.: Oi mãe, troquei de número! Preciso de um Pix urgente..."></textarea>
          <p class="contador">{{ texto.length }} / 2000 caracteres</p>
          @if (erroCampo()) { <p class="msg-erro">{{ erroCampo() }}</p> }
          <p class="nota"><app-icon name="lock" [size]="14" /> Não cole senhas nem dados do cartão.</p>

          <button class="btn btn-ia" type="submit" [disabled]="!texto.trim() || carregando()">
            <app-icon name="sparkles" [size]="18" /> {{ carregando() ? 'Analisando, pode levar alguns segundos...' : 'Analisar com IA' }}
          </button>
        </form>
        @if (erroGeral()) { <p class="msg-erro">{{ erroGeral() }}</p> }
      </main>
    } @else {
      <app-topbar titulo="Resultado da análise" voltarPara="/analisar" />
      <main class="conteudo">
        <app-risco-card [risco]="resultado()!.risco">{{ resultado()!.dica }}</app-risco-card>

        @if (resultado()!.sinais.length) {
          <section class="card">
            <h3>Sinais encontrados</h3>
            <ul class="lista-sinais">
              @for (s of resultado()!.sinais; track s) {
                <li><app-icon name="alert" [size]="18" color="#C62F3B" /> {{ s }}</li>
              }
            </ul>
          </section>
        }

        @if (resultado()!.aviso) {
          <p class="aviso"><app-icon name="info" [size]="16" /> {{ resultado()!.aviso }}</p>
        }
        <p class="nota"><app-icon name="sparkles" [size]="14" color="#6B4EE6" />
          {{ resultado()!.motor === 'GEMINI' ? 'Análise feita por IA (Gemini).' : 'Análise feita por regras locais.' }} Pode errar: confirme sempre por outro canal.</p>

        <button class="btn btn-ia" (click)="nova()"><app-icon name="redo" [size]="18" /> Analisar outra mensagem</button>
        <a class="btn btn-secundario" routerLink="/"><app-icon name="home" [size]="18" /> Voltar ao início</a>
      </main>
    }
  `,
})
export class AnalisarComponent {
  private api = inject(ApiService);
  texto = '';
  carregando = signal(false);
  resultado = signal<AnaliseMensagemResponse | null>(null);
  erroGeral = signal('');
  erroCampo = signal('');

  analisar() {
    this.erroGeral.set(''); this.erroCampo.set('');
    this.carregando.set(true);
    this.api.analisarMensagem(this.texto.trim()).subscribe({
      next: (r) => { this.resultado.set(r); window.scrollTo(0, 0); this.carregando.set(false); },
      error: (e: HttpErrorResponse) => {
        const { geral, campos } = lerErro(e);
        this.erroCampo.set(campos['texto'] ?? '');
        this.erroGeral.set(campos['texto'] ? '' : geral);
        this.carregando.set(false);
      },
    });
  }

  nova() { this.resultado.set(null); this.texto = ''; }
}
