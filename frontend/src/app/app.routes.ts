import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home.component';
import { ConsultarComponent } from './pages/consultar.component';
import { AnalisarComponent } from './pages/analisar.component';
import { AutodiagnosticoComponent } from './pages/autodiagnostico.component';
import { DenunciarComponent } from './pages/denunciar.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'Pix Seguro' },
  { path: 'consultar', component: ConsultarComponent, title: 'Consultar chave · Pix Seguro' },
  { path: 'analisar', component: AnalisarComponent, title: 'Analisar mensagem · Pix Seguro' },
  { path: 'autodiagnostico', component: AutodiagnosticoComponent, title: 'Autodiagnóstico · Pix Seguro' },
  { path: 'denunciar', component: DenunciarComponent, title: 'Denunciar chave · Pix Seguro' },
  { path: '**', redirectTo: '' },
];
