import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Inicio } from './inicio';

describe('Inicio', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inicio],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe mostrar el backend disponible cuando el health responde UP', async () => {
    const fixture = TestBed.createComponent(Inicio);
    fixture.detectChanges();

    httpMock.expectOne('/actuator/health').flush({ status: 'UP' });
    await fixture.whenStable();
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Disponible');
  });

  it('debe mostrar el backend no disponible si la petición falla', async () => {
    const fixture = TestBed.createComponent(Inicio);
    fixture.detectChanges();

    httpMock
      .expectOne('/actuator/health')
      .flush(null, { status: 503, statusText: 'Service Unavailable' });
    await fixture.whenStable();
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('No disponible');
  });
});
