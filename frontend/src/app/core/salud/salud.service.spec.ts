import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EstadoSalud } from './estado-salud';
import { SaludService } from './salud.service';

describe('SaludService', () => {
  let service: SaludService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SaludService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe consultar el health del backend', () => {
    const esperado: EstadoSalud = { status: 'UP', groups: ['liveness', 'readiness'] };
    let recibido: EstadoSalud | undefined;

    service.obtenerEstado().subscribe((estado) => (recibido = estado));

    const peticion = httpMock.expectOne('/actuator/health');
    expect(peticion.request.method).toBe('GET');
    peticion.flush(esperado);

    expect(recibido).toEqual(esperado);
  });
});
