import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from './icon.component';
import { Risco } from '../api.service';

/** Barra superior com voltar + título (padrão das telas internas do protótipo). */
@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, IconComponent],
  template: `
    <header class="topbar">
      <a class="voltar" [routerLink]="voltarPara()" aria-label="Voltar"><app-icon name="back" /></a>
      <h1>{{ titulo() }}</h1>
    </header>
  `,
})
export class TopbarComponent {
  titulo = input.required<string>();
  voltarPara = input<string>('/');
}

/** Card colorido de risco (verde / âmbar / vermelho). */
@Component({
  selector: 'app-risco-card',
  standalone: true,
  imports: [IconComponent],
  template: `
    <section class="risco" [class]="'risco risco-' + risco().toLowerCase()">
      <div class="risco-icone"><app-icon [name]="risco() === 'BAIXO' ? 'check' : 'alert'" [size]="28" /></div>
      <p class="risco-titulo">Risco {{ rotulo() }}</p>
      <p class="risco-texto"><ng-content /></p>
    </section>
  `,
})
export class RiscoCardComponent {
  risco = input.required<Risco>();
  rotulo = () => ({ BAIXO: 'baixo', MEDIO: 'médio', ALTO: 'alto' })[this.risco()];
}
