import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EstadoSalud } from './estado-salud';

/**
 * Consulta el health del backend a través del proxy de desarrollo.
 */
@Injectable({ providedIn: 'root' })
export class SaludService {
  private readonly http = inject(HttpClient);

  obtenerEstado(): Observable<EstadoSalud> {
    return this.http.get<EstadoSalud>('/actuator/health');
  }
}
