package com.andrecarvalho.projeto.services;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.andrecarvalho.projeto.domain.Cidade;
import com.andrecarvalho.projeto.domain.Cliente;
import com.andrecarvalho.projeto.domain.Endereco;
import com.andrecarvalho.projeto.domain.enums.Perfil;
import com.andrecarvalho.projeto.domain.enums.TipoCliente;
import com.andrecarvalho.projeto.dto.ClienteDTO;
import com.andrecarvalho.projeto.dto.ClienteNewDTO;
import com.andrecarvalho.projeto.repositories.ClienteRepository;
import com.andrecarvalho.projeto.repositories.EnderecoRepository;
import com.andrecarvalho.projeto.security.UserSS;
import com.andrecarvalho.projeto.services.exceptions.AuthorizationException;
import com.andrecarvalho.projeto.services.exceptions.DataIntegrityException;
import com.andrecarvalho.projeto.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired
	private BCryptPasswordEncoder pe;
	
	@Autowired
	private S3Service s3Service;

	public Cliente find(Integer id) {
		UserSS user = UserService.authenticated();
		if(user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getIde())) {
			throw new AuthorizationException("Acesso negado");
		}
		 Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
		 "Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
		} 
	
	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}
	
	public Cliente update (Cliente obj) {
		Cliente newObj = find(obj.getId());
		UpdateData(newObj, obj);
		return repo.save(newObj);
	}
	
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
	} catch(DataIntegrityViolationException e) {
		throw new DataIntegrityException("Não é possível excluir um cliente que possui pedidos pendentes");
	}
	}
	
	public List<Cliente> findAll(){
		return repo.findAll();
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction),orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);

	}
	
	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(), TipoCliente.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2()!=null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3()!=null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}
	
	private void UpdateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
	
	public URI uploadProfilePicture(MultipartFile multipartFile) {
		return s3Service.uploadFile(multipartFile);
	}
}