package hash_dinamica.implementacoes;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.function.Function;

import hash_dinamica.*;

/**
 * Estrutura de hashing dinâmico para indexamento de registros.
 * 
 * @author Axell Brendow ( https://github.com/axell-brendow )
 */

public class HashDinamicaIntLong extends HashDinamica<Serializavel, Serializavel>
{
	Diretorio<TIPO_DAS_CHAVES> diretorio;
	Buckets<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> buckets;
	 
	// auxilia no controle de recursividade infinita
	// da função tratarBucketCheio() juntamente com a
	// função inserir()
	private boolean chamadaInterna = false;
	private int numeroDeChamadas = 0;
	
	/**
	 * Cria um objeto que gerencia uma hash dinâmica.
	 * 
	 * @param nomeDoArquivoDoDiretorio Nome do arquivo previamente usado para o diretório.
	 * @param nomeDoArquivoDosBuckets Nome do arquivo previamente usado para os buckets.
	 * Caso o arquivo não tenha sido criado ainda, ele será criado com este nome.
	 * @param numeroDeRegistrosPorBucket Numero de registros por bucket caso o arquivo
	 * não tenha sido criado ainda.
	 * @param quantidadeMaximaDeBytesParaAChave Tamanho máximo que a chave pode gastar.
	 * @param quantidadeMaximaDeBytesParaODado Tamanho máximo que o dado pode gastar.
	 * @param construtorDaChave Construtor da chave. É necessário que a chave tenha um
	 * construtor sem parâmetros.
	 * @param construtorDoDado Construtor do dado. É necessário que o dado tenha um
	 * construtor sem parâmetros.
	 * @param funcaoHash Função de dispersão (hash) que será usada para as chaves. É
	 * importante ressaltar que essa função só precisa gerar valores aleatórios, não
	 * importando o tamanho dos valores.
	 */
	
	public HashDinamicaIntLong(
		String nomeDoArquivoDoDiretorio,
		String nomeDoArquivoDosBuckets,
		int numeroDeRegistrosPorBucket,
		short quantidadeMaximaDeBytesParaAChave,
		short quantidadeMaximaDeBytesParaODado,
		Constructor<TIPO_DAS_CHAVES> construtorDaChave,
		Constructor<TIPO_DOS_DADOS> construtorDoDado,
		Function<TIPO_DAS_CHAVES, Integer> funcaoHash)
	{
		diretorio = new Diretorio<>(nomeDoArquivoDoDiretorio, funcaoHash);
		
		buckets = new Buckets<>(
			nomeDoArquivoDosBuckets,
			numeroDeRegistrosPorBucket,
			quantidadeMaximaDeBytesParaAChave,
			quantidadeMaximaDeBytesParaODado,
			construtorDaChave,
			construtorDoDado);
	}
	
	/**
	 * Procura todos os registros com uma chave específica e gera
	 * uma lista com os dados correspondentes a essas chaves.
	 * 
	 * @param chave Chave a ser procurada.
	 * 
	 * @return lista com os dados correspondentes às chaves.
	 */
	
	public ArrayList<TIPO_DOS_DADOS> listarDadosComAChave(TIPO_DAS_CHAVES chave)
	{
		return buckets.listarDadosComAChave(chave, diretorio.obterEndereçoDoBucket(chave));
	}
	
	/**
	 * Insere todos os registros ativados de um bucket na
	 * hash dinâmica.
	 * 
	 * @param bucket Bucket com os registros a serem inseridos.
	 */
	
	public void inserirElementosDe(Bucket<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> bucket)
	{
		for (int i = 0; i < buckets.numeroDeRegistrosPorBucket; i++)
		{
			RegistroDoIndice<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> registro =
				bucket.obterRegistro(i);
			
			if (registro.lapide == RegistroDoIndice.REGISTRO_ATIVADO)
			{
				inserir(registro.chave, registro.dado);
			}
		}
	}
	
	/**
	 * Cuida do processo que precisa ser feito quando tenta-se
	 * inserir um registro num bucket que está cheio.
	 * 
	 * @param enderecoDoBucket Endereço do bucket que está cheio.
	 * @param resultado Resultado do método
	 * {@link Buckets#inserir(Serializavel, Serializavel, long)}.
	 * @param chave Chave do registro não inserido.
	 * @param dado Dado do registro não inserido.
	 */
	
	private void tratarBucketCheio(
		long enderecoDoBucket,
		byte resultado,
		TIPO_DAS_CHAVES chave,
		TIPO_DOS_DADOS dado)
	{
		// conta quantas vezes esta função foi chamada por uma função
		// inserir() que tenha sido chamada por esta função.
		// (desculpe-me pela recursividade, mas é isso mesmo)
		numeroDeChamadas = ( chamadaInterna ? numeroDeChamadas + 1 : 0 );
		
		// se o numero de chamadas for 2, ou seja, se esta função tiver
		// sido chamada pela própria classe duas vezes, há uma grande
		// probabilidade de o processo recursivo ser infinito, portanto,
		// não rodo a função mais.
		if (numeroDeChamadas < 2)
		{
			// profundidade local do bucket igual à profundidade global do diretório
			if (resultado == diretorio.obterProfundidadeGlobal())
			{
				diretorio.duplicar();
			}
			
			Bucket<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> bucketExcluido =
				buckets.resetarBucket(enderecoDoBucket);
			
			long enderecoDoNovoBucket =
				buckets.criarBucket( (byte) (resultado + 1) );
			
			diretorio.atribuirPonteiroNoIndice
			(
				diretorio.obterIndiceDoUltimoPonteiroAlterado() + 1,
				enderecoDoNovoBucket
			);
			
			chamadaInterna = true;
			inserirElementosDe(bucketExcluido);
			
			inserir(chave, dado);
			chamadaInterna = false;
		}
		
		else
		{
			Main.println(
				"Inclusão ignorada. A chave que deseja-se inserir, juntamente\n" +
				"com outras existentes, gera duplicação infinita do diretório.\n" +
				"Experimente aumentar a quantidade de registros por bucket.\n\n" +
				"Chave:\n" +
				chave + "\n" +
				"Dado:\n" +
				dado
			);
			
			numeroDeChamadas = 0;
		}
	}
	
	/**
	 * Tenta inserir a chave e o dado na hash dinâmica.
	 * 
	 * @param chave Chave a ser inserida.
	 * @param dado Dado que corresponde à chave.
	 * 
	 * @return {@code true} se a chave e o dado forem inseridos.
	 * Caso contrário, {@code false}.
	 */
	
	public boolean inserir(TIPO_DAS_CHAVES chave, TIPO_DOS_DADOS dado)
	{
		boolean sucesso = false;

		long enderecoDoBucket = diretorio.obterEndereçoDoBucket(chave);
		
		if (enderecoDoBucket != -1)
		{
			byte resultado = buckets.inserir(chave, dado, enderecoDoBucket);
			
			if (resultado == -1) // inserção bem sucedida
			{
				sucesso = true;
			}
			
			// bucket cheio, resultado será igual à profundidade local do bucket
			else if (resultado >= 0)
			{
				tratarBucketCheio(enderecoDoBucket, resultado, chave, dado);
				
				sucesso = true;
			}
		}
		
		return sucesso;
	}
}
