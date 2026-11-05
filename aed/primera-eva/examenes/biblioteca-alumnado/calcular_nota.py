#!/usr/bin/env python3
"""Evalúa JUnit Surefire y JavaDoc de las interfaces, sin modificar el código alumno."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
REPORTS=ROOT/'target'/'surefire-reports'
SRC=ROOT/'src'/'main'/'java'/'es'/'ejemplo'/'biblioteca'/'dao'
BLOQUES={'Libro':'LibroDaoTest','Socio':'SocioDaoTest','Prestamo':'PrestamoDaoTest'}
EXTRAS={
 'Socio': {'buscarPorEmailCoincideTest','buscarPorEmailSinCoincidenciaTest','listarAltasDesdeCoincideTest','listarAltasDesdeSinCoincidenciaTest'},
 'Prestamo': {'contarPrestamosPorSocioCoincideTest','contarPrestamosPorSocioSinPrestamosTest','contarPrestamosPorSocioVariosTest','listarPrestamosPorAutorCoincideTest','listarPrestamosPorAutorSinCoincidenciaTest'},
}
# En estas pruebas la interfaz se usa con consultas avanzadas: no son CRUD.
EXTRAS['Socio'].add('interfazDAOPermiteFiltrarTest')
EXTRAS['Prestamo'].add('interfazDAOPermiteConsultasTest')
PESOS={'Libro':2.0,'Socio':2.0,'Prestamo':2.0,'socios_consultas':1.5,'prestamos_group':0.95,'prestamos_sub':0.95,'documentacion':0.60}

def normales(s):
 return s.translate(str.maketrans('áéíóúÁÉÍÓÚñÑ','aeiouAEIOUnN')).lower()

PALABRAS_CLAVE_API = {
    'listar': [('listar', 'lista', 'listado')],
    'buscarPorId': [('id', 'identificador')],
    'insertar': [('insertar', 'inserta', 'insercion', 'registrar')],
    'actualizar': [('actualizar', 'actualiza', 'modificar', 'cambiar')],
    'eliminar': [('eliminar', 'elimina', 'borrar', 'suprimir')],
    'buscarPorEmail': [('email', 'correo')],
    'listarAltasDesde': [('fecha',), ('alta', 'altas')],
    'contarPrestamosPorSocio': [('prestamo', 'prestamos'), ('socio', 'socios')],
    'listarPrestamosPorAutor': [('autor',), ('prestamo', 'prestamos')],
}

def palabras_clave_presentes(nombre_metodo, descripcion):
    texto = normales(descripcion)
    grupos = PALABRAS_CLAVE_API.get(nombre_metodo, [])
    return bool(grupos) and all(
        any(re.search(r'\b' + re.escape(palabra) + r'\b', texto) for palabra in grupo)
        for grupo in grupos
    )

def doc_metodos(archivo):
 if not archivo.exists():return []
 txt=archivo.read_text(encoding='utf8')
 patron=re.compile(r'(?P<javadoc>/\*\*[\s\S]*?\*/)?\s*(?:public\s+)?(?P<retorno>[\w<>\[\], ?]+)\s+(?P<metodo>\w+)\s*\((?P<params>[^)]*)\)\s*;',re.M)
 resultado=[]
 for m in patron.finditer(txt):
  jd=m['javadoc'] or ''; params=[p.strip().split()[-1] for p in m['params'].split(',') if p.strip()]
  contenido=normales(re.sub(r'[@*]',' ',re.sub(r'^/\*\*|\*/$','',jd.strip())))
  pts=0
  descripcion = re.split(r'@(?:param|return|throws|see|since|deprecated)\b', jd, maxsplit=1)[0]
  descripcion = re.sub(r'^/\*\*|\*/$', '', descripcion.strip())
  if len(' '.join(contenido.split()))>=25 and m['metodo'].lower() not in contenido.strip()[:10]:pts+=.40
  if all(re.search(r'@param\s+'+re.escape(p)+r'\b',jd) for p in params):pts+=.25
  if m['retorno'].strip()=='void' or '@return' in jd:pts+=.20
  if palabras_clave_presentes(m['metodo'], descripcion):pts+=.15
  resultado.append((m['metodo'],pts))
 return resultado

def lectura_tests():
 resultados={k:{} for k in BLOQUES}
 if not REPORTS.exists():return resultados
 for file in REPORTS.glob('TEST-*.xml'):
  try:root=ET.parse(file).getroot()
  except ET.ParseError:continue
  nombre=root.get('name','')
  for bloque,clase in BLOQUES.items():
   if nombre.split('.')[-1]!=clase:continue
   for caso in root.iter('testcase'):
    metodo=caso.get('name','').split('(')[0]
    correcto=not any(child.tag in ('failure','error','skipped') for child in caso)
    resultados[bloque][metodo]=correcto
 return resultados

def nota():
 pruebas=lectura_tests(); partes=[]; lineas=['=== NOTA POR BLOQUES ===','Comando: mvn clean verify -Pnota','']
 todos_crud=True
 for b in BLOQUES:
  resultados=pruebas[b]; especial=EXTRAS.get(b,set()); crud={k:v for k,v in resultados.items() if k not in especial}
  esperados={'Libro':16,'Socio':11,'Prestamo':10}[b]
  superados=sum(crud.values()); completos=len(crud)==esperados and superados==esperados
  todos_crud &= completos
  nota_crud=PESOS[b]*superados/esperados
  partes.append(nota_crud)
  lineas.extend([f'=== {b.upper()} ===',f'CRUD: {superados}/{esperados} tests superados; aportación {nota_crud:.2f}/2.00',f'CRUD completo: {"sí" if completos else "no"}',''])
 socio=sum(pruebas['Socio'].get(k,False) for k in EXTRAS['Socio'])
 grupo=sum(pruebas['Prestamo'].get(k,False) for k in EXTRAS['Prestamo'] if k.startswith('contar') or k.startswith('interfaz'))
 sub=sum(pruebas['Prestamo'].get(k,False) for k in EXTRAS['Prestamo'] if k.startswith('listar'))
 # Las pruebas especiales se ponderan proporcionalmente por el total esperado.
 n_socio=1.5*socio/5; n_grupo=.95*grupo/4; n_sub=.95*sub/2
 partes += [n_socio,n_grupo,n_sub]
 docs=[]
 for nombre in ('Libro','Socio','Prestamo'):
  archivo=SRC/f'I{nombre}Dao.java'; docs.extend((archivo.name,metodo,pt) for metodo,pt in doc_metodos(archivo))
 doc_media=sum(x[2] for x in docs)/19 if len(docs)==19 else 0.0
 n_doc=.6*doc_media if todos_crud else 0
 partes.append(n_doc)
 lineas.extend(['=== CONSULTAS ADICIONALES ===',f'SocioDao: {socio}/5 tests; {n_socio:.2f}/1.50',f'PrestamoDao método adicional 1: {grupo}/4 tests; {n_grupo:.2f}/0.95',f'PrestamoDao método adicional 2: {sub}/2 tests; {n_sub:.2f}/0.95','', '=== DOCUMENTACIÓN ===',f'JavaDoc: {len(docs)}/19 métodos detectados; calidad {doc_media*10:.2f}/10',f'CRUD completos: {"sí" if todos_crud else "no"}; aportación {n_doc:.2f}/0.60'])
 for archivo,metodo,pt in docs:
  if pt<.999:lineas.append(f'- {archivo}.{metodo}: {pt:.2f}/1.00')
 total=round(sum(partes),2)
 lineas.extend(['',f'NOTA FINAL: {total:.2f}/10.00',''])
 informe='\n'.join(lineas)
 target=ROOT/'target';target.mkdir(exist_ok=True)
 (target/'notas_por_bloques.txt').write_text(informe,encoding='utf8')
 print(informe)
 return 0
if __name__=='__main__':raise SystemExit(nota())
