/**
 * @file serializavel.hpp
 * @author Axell Brendow ( https://github.com/axell-brendow )
 * @brief Arquivo destinado a guardar classes úteis para a serialização de objetos.
 * 
 * @copyright Copyright (c) 2019 Axell Brendow Batista Moreira
 */

#include "tipos.hpp"
#include "../streams/DataOutputStream.cpp"

/**
 * @brief Classe abstrata que todo objeto serializável deve herdar.
 * 
 * @see [Interfaces C++](https://www.tutorialspoint.com/cplusplus/cpp_interfaces)
 * @see [Serialização - Wikipédia](https://pt.wikipedia.org/wiki/Serializa%C3%A7%C3%A3o)
 */

class Serializavel
{
    public:
        /**
         * @brief Calcula o tamanho máximo, em bytes, que a entidade pode gastar.
         * 
         * @return int Retorna o tamanho máximo, em bytes, que a entidade pode gastar.
         */

        virtual int obterTamanhoMaximoEmBytes() = 0;
        
        /**
         * @brief Gera um vetor de bytes com os dados da entidade.
         * 
         * @return vetor_de_bytes Retorna o vetor de bytes com os dados da entidade.
         */

        virtual vetor_de_bytes gerarBytes() = 0;
        
        /**
         * @brief Lê e interpreta o vetor recebido restaurando a entidade.
         */
        
        virtual void lerBytes(vetor_de_bytes vetor) = 0;
};

ostream &operator<<(ostream &ostream, Serializavel &serializavel)
{
    DataOutputStream out(serializavel.gerarBytes());

    return ostream << out;
}