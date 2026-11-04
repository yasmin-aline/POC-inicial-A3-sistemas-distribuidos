import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, DenunciaResponse, lerErro } from '../api.service';
import { IconComponent } from '../shared/icon.component';
import { TopbarComponent } from '../shared/ui';

@Component({
  selector: 'app-denunciar',
  standalone: true,
  imports: [FormsModule, RouterLink, IconComponent, TopbarComponent],
  template: `
    @if (!resultado()) {
      <app-topbar titulo="Denunciar chave" />
      <main class="conteudo">
        <h2>Conte o que aconteceu</h2>
        <p class="sub">Sua denúncia alerta quem consultar essa chave.</p>

        <form (ngSubmit)="enviar()">
          <label for="chave">Chave Pix</label>
          <div class="campo" [class.erro]="erros()['chave']">
            <input id="chave" name="chave" [(ngModel)]="chave" maxlength="140" placeholder="CPF, celular, e-mail ou chave aleatória" />
          </div>
          @if (erros()['chave']) { <p class="msg-erro">{{ erros()['chave'] }}</p> }

          <label>Você já fez o Pix?</label>
          <div class="segmento largo" role="group" aria-label="Você já fez o Pix?">
            <button type="button" [class.sim]="jaFezPix === true" (click)="jaFezPix = true">Sim, já paguei</button>
            <button type="button" [class.nao]="jaFezPix === false" (click)="jaFezPix = false">Não paguei</button>
          </div>

          <label for="descricao">O que aconteceu? (opcional)</label>
          <textarea id="descricao" name="descricao" [(ngModel)]="descricao" maxlength="500" rows="4"
                    placeholder="Ex.: Disseram que trocaram de número e pediram Pix urgente."></textarea>
          @if (erros()['descricao']) { <p class="msg-erro">{{ erros()['descricao'] }}</p> }
          <p class="nota"><app-icon name="lock" [size]="14" /> Sua denúncia é anônima. Guardamos só um código da chave, nunca a chave em si.</p>

          <button class="btn btn-perigo" type="submit" [disabled]="!chave.trim() || jaFezPix === null || carregando()">
            <app-icon name="flag" [size]="18" /> {{ carregando() ? 'Enviando...' : 'Enviar denúncia' }}
          </button>
        </form>
        @if (erroGeral()) { <p class="msg-erro">{{ erroGeral() }}</p> }
      </main>
    } @else {
      <app-topbar titulo="Denúncia enviada" />
      <main class="conteudo">
        <section class="card centro">
          <span class="chip grande" style="background:#E6F6EE"><app-icon name="check" [size]="38" color="#1E8E5A" /></span>
          <h2>Denúncia registrada</h2>
          <p class="sub">Esta chave agora tem {{ resultado()!.totalDenunciasChave }} denúncia(s).</p>
        </section>

        <section class="info">
          <p class="info-titulo"><app-icon name="info" [size]="18" color="#1E5BD8" />
            {{ resultado()!.proximoPasso === 'PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO' ? 'Já fez o Pix? Faça isto agora' : 'Próximos passos' }}</p>
          <ul class="lista-passos">
            @for (o of resultado()!.orientacoes; track o) { <li>{{ o }}</li> }
          </ul>
        </section>

        <a class="btn btn-primario" routerLink="/consultar"><app-icon name="search" [size]="18" /> Consultar outra chave</a>
        <a class="btn btn-secundario" routerLink="/"><app-icon name="home" [size]="18" /> Voltar ao início</a>
      </main>
    }
  `,
})
export class DenunciarComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  chave = '';
  descricao = '';
  jaFezPix: boolean | null = null;
  carregando = signal(false);
  resultado = signal<DenunciaResponse | null>(null);
  erroGeral = signal('');
  erros = signal<Record<string, string>>({});

  ngOnInit() {
    this.chave = this.route.snapshot.queryParamMap.get('chave') ?? '';
  }

  enviar() {
    this.carregando.set(true); this.erroGeral.set(''); this.erros.set({});
    this.api.denunciar({ chave: this.chave.trim(), descricao: this.descricao.trim() || null, jaFezPix: !!this.jaFezPix }).subscribe({
      next: (r) => { this.resultado.set(r); window.scrollTo(0, 0); this.carregando.set(false); },
      error: (e: HttpErrorResponse) => {
        const { geral, campos } = lerErro(e);
        this.erros.set(campos);
        this.erroGeral.set(Object.keys(campos).length ? '' : geral);
        this.carregando.set(false);
      },
    });
  }
}
