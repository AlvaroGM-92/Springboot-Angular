import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { SaludService } from '../../core/salud/salud.service';

type Estado = 'cargando' | 'arriba' | 'abajo';

@Component({
  selector: 'app-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss',
})
export class Inicio {
  private readonly salud = inject(SaludService);

  protected readonly estado = signal<Estado>('cargando');
  protected readonly ultimaComprobacion = signal<Date | null>(null);

  constructor() {
    this.comprobar();
  }

  protected comprobar(): void {
    this.estado.set('cargando');
    this.salud.obtenerEstado().subscribe({
      next: (respuesta) => {
        this.estado.set(respuesta.status === 'UP' ? 'arriba' : 'abajo');
        this.ultimaComprobacion.set(new Date());
      },
      error: () => {
        this.estado.set('abajo');
        this.ultimaComprobacion.set(new Date());
      },
    });
  }
}
