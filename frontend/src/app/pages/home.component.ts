import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, IconComponent],
  template: `
    <section class="hero">
      <div class="marca">
        <span class="marca-icone"><app-icon name="shield" [size]="20" color="#1E5BD8" /></span>
        <span>Pix Seguro</span>
      </div>
      <h1>Vai fazer um Pix?</h1>
      <p>Confira antes de pagar. Leva menos de 1 minuto.</p>
    </section>

    <main class="conteudo">
      @for (f of funcoes; track f.rota) {
        <a class="funcao" [routerLink]="f.rota">
          <span class="chip" [style.background]="f.fundo"><app-icon [name]="f.icone" [size]="22" [color]="f.cor" /></span>
          <span class="funcao-texto">
            <strong>{{ f.titulo }}</strong>
            <small>{{ f.sub }}</small>
          </span>
          <app-icon name="chevR" [size]="18" color="#8A94A6" />
        </a>
      }

      <aside class="dica">
        <p class="dica-titulo"><app-icon name="bulb" [size]="18" color="#C98A00" /> Dica do dia</p>
        <p>Recebeu pedido de Pix urgente de um "parente" com número novo? Ligue no número antigo antes de pagar.</p>
      </aside>
    </main>
  `,
})
export class HomeComponent {
  funcoes = [
    { rota: '/consultar', icone: 'search', cor: '#1E5BD8', fundo: '#E8F0FF', titulo: 'Consultar chave Pix', sub: 'Veja se a chave tem denúncias' },
    { rota: '/analisar', icone: 'sparkles', cor: '#6B4EE6', fundo: '#F0ECFF', titulo: 'Analisar mensagem', sub: 'A IA aponta sinais de golpe' },
    { rota: '/autodiagnostico', icone: 'clipboard', cor: '#1E8E5A', fundo: '#E6F6EE', titulo: 'Autodiagnóstico', sub: '5 perguntas rápidas de sim ou não' },
    { rota: '/denunciar', icone: 'flag', cor: '#C62F3B', fundo: '#FDE8EA', titulo: 'Denunciar chave', sub: 'Ajude a proteger outras pessoas' },
  ];
}
