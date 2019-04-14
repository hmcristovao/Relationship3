# Relationship (versão 3)

Sistema de recuperação de informação sobre a base de dados ligados da DBpedia usando técnicas de análise de redes complexas e visualização de conhecimento por intermédio de amapas conceituais.

## Versão 3:

Essa versão usa a biblioteca antiga do Gephi Tool Kit (de 2013). Possui acréscimo de interface gráfica para saída dos mapas conceituais intermediários, porém ainda não integrada no fluxo de execução default. 
Em desenvolvimento:
* interface gráfica para entrada dos termos de busca com auto-complete pela base de dados ligados da DBpedia offline.
* uso de conteinerização.

## Instalação:

Obs.: 
* necessária conta no GitHub e o compartilhamento do projeto com essa conta.
* anotações realizadas com Eclipse versão Photon (version 4.8 em set-2018)

### 1º) Intalação do Eclipse
http://www.eclipse.org
Pacote: "Eclipse IDE for Java EE Developers" pois já vem com o EGIT e com o Marketplace (módulo que facilita a instalação de plugins).

### 2º) Instalação do Plugin Javacc no Eclipse
'Help' - 'Eclipse Marketplace' - procure por Javacc e proceda a instalação.

### 3º) Importação do projeto Relationship que está no GitHub
No Eclipse: File > Import > Git > Projects from Git > Next > Clone URI > Next
URI: https://github.com/hmcristovao/Relationship3
(preencha o que estiver faltando...)
> Next
Na janela Branch Selection, selecione master e todos os branches.
> Next
Na janela - Local Destination: 
Directory: (informe o diretório local base onde será criado o diretório que irá receber o projeto)
Initial branch: (escolha o branch mais atual)
> Next
> Finish
Obs.: o download completo demora muitos minutos.
> Import existing Eclipse projects > Next > Finish

### 4º) Tornar as bibliotecas visíveis pelo projeto (talvez isso não seja necessário, pois é possível que todas as libs estejam linkadas):
> Project > Properties > Java Build Path > Libraries > Add JARs…
abra o Projeto Relationship e o diretório lib
selecione todos os arquivos JARs de cada biblioteca (somente arquivos JAR)
> OK > Apply

### 5ª) Compilar normalmente (a primeira vez demora bastante)

### 6º) Execução:
* na janela “Select Java Aplication”, indique “Main.mainProcess”
* configure o diretório de saída no Config.txt
// Base directory to creation of output files:
baseDirectory = C:\\Users\\Henrique\\Documents\\Relationship
Observe que no Linux usa-se // e no Windows \\

### 7º) Visualize o mapa conceitual final por intermédio do arquivo de extensão CLX.
Ele pode ser aberto pelo CmapTools.
Use o atalho CTRL+L para uma sugestão inicial de formatação.

## Autor:

* Henrique Monteiro Cristovão - hmcristovao@gmail.com

## Colaboradores:

* Iago Pires Duarte - iagoduarte16@gmail.com 
* Luis Henrique Gundes Valim - henriquegundes@outlook.com 
* Fabiano Amaral Freitas - fabianin.amaral@gmail.com 

