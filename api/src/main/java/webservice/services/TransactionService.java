package webservice.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webservice.dto.TransactionDTO;
import webservice.dto.TransactionRequest;
import webservice.entities.Transaction;
import webservice.repositories.TransactionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private TransactionRepository transactionRepository;
    private ModelMapper modelMapper;

    @Autowired
    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public List<TransactionDTO> getAllTransactionsByUserId(int userId) {
        return transactionRepository.findAllByUserId(userId).stream().map(entity -> modelMapper.map(entity, TransactionDTO.class)).collect(Collectors.toList());
    }

    public List<TransactionDTO> getAllUserIncome(int userId) {
        return null;
    }

    public List<TransactionDTO> getAllUserExpenses(int userId) {
        return null;
    }

    public boolean deleteTransaction(int transactionId) {
        return false;
    }

    public TransactionDTO updateTransaction(TransactionDTO transaction) {
        return null;
    }

    public TransactionDTO insertTransaction(TransactionRequest transaction) {
        return null;
    }

    public TransactionDTO getTransaction(int transactionId) {
        return modelMapper.map(transactionRepository.findById(transactionId), TransactionDTO.class);
    }

    public List<TransactionDTO> getAllExpense() {
        return null;
    }

    public List<TransactionDTO> getAllIncome() {
        return null;
    }

    public List<TransactionDTO> getAllTransactions() {
        return ((List<Transaction>) transactionRepository.findAll()).stream().map(entity -> modelMapper.map(entity, TransactionDTO.class)).collect(Collectors.toList());
    }
}
