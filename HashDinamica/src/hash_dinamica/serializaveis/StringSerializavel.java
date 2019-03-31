package hash_dinamica.serializaveis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringSerializavel extends SerializavelAbstract
{
	public static final int PADRAO_TAMANHO_MAXIMO_EM_BYTES = 300;
	
	public String dado;
	public int tamanhoMaximoEmBytes;
	
	public StringSerializavel(String dado, int tamanhoMaximoEmBytes)
	{
		this.dado = dado;
		this.tamanhoMaximoEmBytes = tamanhoMaximoEmBytes;
	}
	
	public StringSerializavel(String dado)
	{
		this(dado, PADRAO_TAMANHO_MAXIMO_EM_BYTES);
	}
	
	public StringSerializavel()
	{
		this("");
	}

	@Override
	public int obterTamanhoMaximoEmBytes()
	{
		return tamanhoMaximoEmBytes + Integer.BYTES;
	}

	@Override
	public byte[] obterBytes()
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		
		try
		{
			dataOutputStream.writeUTF(dado);
			dataOutputStream.writeInt(tamanhoMaximoEmBytes);
			dataOutputStream.close();
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return byteArrayOutputStream.toByteArray();
	}

	@Override
	public void lerBytes(byte[] bytes)
	{
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		
		try
		{
			dado = dataInputStream.readUTF();
			tamanhoMaximoEmBytes = dataInputStream.readInt();
			dataInputStream.close();
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString()
	{
		return dado;
	}
}
