package webservice.services;

import com.querydsl.core.types.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import webservice.dto.RegistrationDTO;
import webservice.dto.UserDTO;
import webservice.entities.QUser;
import webservice.entities.User;
import webservice.exceptions.BadParameterException;
import webservice.exceptions.DuplicateEntryException;
import webservice.exceptions.ResourceNotFoundException;
import webservice.repositories.UserRepository;
import webservice.services.interfaces.HashService;
import webservice.util.mappers.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserMapper mockedUserMapper;
    @Mock
    private UserRepository mockedUserRepository;
    @Mock
    private HashService mockedHashService;
    @Mock
    private Predicate mockedPredicate;
    @Mock
    private Pageable mockedPageable;
    @Mock
    private Page<User> userPage;
    @Mock
    private List<User> userList;
    @Mock
    private UserDTO mockedUserDTO;
    @Mock
    private RegistrationDTO mockedRegistrationDTO;
    @Mock
    private User mockedUser;

    private Optional<User> emptyOptionalUser;
    private Optional<User> optionalUser;

    private final String username = "test";
    private final String password = "test";
    private final String encodedPassword = "encodedPassword";
    private final String email = "test@test.nl";

    private final int userId = 1;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        emptyOptionalUser = Optional.empty();
        when(mockedUser.getUsername()).thenReturn(username);
        when(mockedUser.getEmail()).thenReturn(email);
        optionalUser = Optional.of(mockedUser);
        when(mockedRegistrationDTO.getUsername()).thenReturn(username);
        when(mockedRegistrationDTO.getEmail()).thenReturn(email);
        when(mockedRegistrationDTO.getPassword()).thenReturn(password);
        when(mockedUserRepository.findAll(mockedPredicate, mockedPageable)).thenReturn(userPage);
        when(userPage.toList()).thenReturn(userList);
        when(mockedHashService.encode(password)).thenReturn(encodedPassword);
        when(mockedUserMapper.mapToUser(mockedRegistrationDTO)).thenReturn(mockedUser);

    }


    @Test
    public void getAllPassesPredicateWithContentToRepositoryWhenPredicateIsInitiallyNull() {
        final Predicate expectedPredicate = QUser.user.id.ne(-1);

        when(mockedUserRepository.findAll(expectedPredicate, mockedPageable)).thenReturn(userPage);

        userService.getAllUsers(null, mockedPageable);

        verify(mockedUserRepository).findAll(expectedPredicate, mockedPageable);
    }

    @Test
    public void getAllPassesCorrectPredicateToRepository() {
        userService.getAllUsers(mockedPredicate, mockedPageable);

        verify(mockedUserRepository).findAll(mockedPredicate, mockedPageable);
    }

    @Test
    public void getAllMapsReturnedUserEntitiesToDTOs() {
        userService.getAllUsers(mockedPredicate, mockedPageable);

        verify(mockedUserMapper).mapToUserDTOList(userList);
    }

    @Test
    public void getAllConvertsResultToList() {
        userService.getAllUsers(mockedPredicate, mockedPageable);

        verify(userPage).toList();
    }

    @Test
    public void getAllReturnsListOfUsers() {
        Assertions.assertNotNull(userService.getAllUsers(mockedPredicate, mockedPageable));
    }

    @Test
    public void getUserReturnsUser() {
        when(mockedUserRepository.findById(userId)).thenReturn(optionalUser);
        when(mockedUserMapper.mapToUserDTO(mockedUser)).thenReturn(mockedUserDTO);

        Assertions.assertNotNull(userService.getUser(userId));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void getUserThrowsResourceNotFoundExceptionWhenFindByIdReturnsFalse() {
        when(mockedUserRepository.findById(userId)).thenReturn(Optional.empty());

        userService.getUser(userId);
    }

    @Test
    public void getUserThrowsCorrectExceptionMessage() {
        final String expectedMessage = "No user found";

        when(mockedUserRepository.findById(userId)).thenReturn(Optional.empty());

        final String actualMessage = Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.getUser(userId)).getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void getUserCallsRepositoryFindById() {
        when(mockedUserRepository.findById(userId)).thenReturn(optionalUser);

        userService.getUser(userId);

        verify(mockedUserRepository).findById(userId);
    }

    @Test
    public void deleteUserCallsRepositoryExistsById() {
        when(mockedUserRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(mockedUserRepository).existsById(userId);
    }

    @Test
    public void deleteUserCallsRepositoryDeleteByIdWhenExistByIdReturnsTrue() {
        when(mockedUserRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(mockedUserRepository).deleteById(userId);
    }

    @Test
    public void deleteUserReturnsTrueWhenUserGetsDeleted() {
        when(mockedUserRepository.existsById(userId)).thenReturn(true);

        assertTrue(userService.deleteUser(userId));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void deleteUserThrowsResourceNotFoundExceptionWhenRepositoryDeleteByIdReturnsFalse() {
        when(mockedUserRepository.existsById(userId)).thenReturn(false);

        userService.deleteUser(userId);
    }

    @Test
    public void deleteUserThrowsCorrectExceptionMessage() {
        final String expectedMessage = "User can not be deleted. The given user does not exist.";

        when(mockedUserRepository.existsById(userId)).thenReturn(false);

        final String actualMessage = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test(expected = BadParameterException.class)
    public void addUserThrowsBadParameterExceptionWhenRegistrationDtoIsNull() {
        userService.addUser(null);
    }

    @Test
    public void addUserBadParameterExceptionThrowsCorrectMessage() {
        final String expectedMessage = "The registration is null";

        final String actualMessage = assertThrows(BadParameterException.class, () -> userService.addUser(null)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test(expected = DuplicateEntryException.class)
    public void addUserRepositoryFindByUsernameThrowsDuplicateEntryExceptionWhenUsernameIsAlreadyPresent() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(optionalUser);

        userService.addUser(mockedRegistrationDTO);
    }

    @Test
    public void addUserRepositoryFindByUsernameThrowsCorrectExceptionMessage() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(optionalUser);
        final String expectedMessage = "Username already exists";

        final String actualMessage = assertThrows(DuplicateEntryException.class, () -> userService.addUser(mockedRegistrationDTO)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test(expected = DuplicateEntryException.class)
    public void addUserRepositoryFindByEmailThrowsDuplicateEntryExceptionWhenUsernameIsAlreadyPresent() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(optionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);

        userService.addUser(mockedRegistrationDTO);
    }

    @Test
    public void addUserRepositoryFindByEmailThrowsCorrectExceptionMessage() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(optionalUser);

        final String expectedMessage = "Email already exists";

        final String actualMessage = assertThrows(DuplicateEntryException.class, () -> userService.addUser(mockedRegistrationDTO)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void addUserRepositoryCallsAndPassesCorrectValueToEncodedPassword() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);

        userService.addUser(mockedRegistrationDTO);

        verify(mockedHashService).encode(password);
    }

    @Test
    public void addUserRepositoryPassesEncodedPasswordToRegistrationDTOSetPassword() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);


        userService.addUser(mockedRegistrationDTO);

        verify(mockedRegistrationDTO).setPassword(encodedPassword);
    }

    @Test
    public void addUserRepositoryMapsRegistrationDTOToUserEntity() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);

        userService.addUser(mockedRegistrationDTO);

        verify(mockedUserMapper).mapToUser(mockedRegistrationDTO);
    }

    @Test
    public void addUserCallsRepositorySaveFunction() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);

        userService.addUser(mockedRegistrationDTO);

        verify(mockedUserRepository).save(mockedUser);
    }

    @Test
    public void addUserMapsSavedUserBackToUserDTO() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.save(mockedUser)).thenReturn(mockedUser);

        userService.addUser(mockedRegistrationDTO);

        verify(mockedUserMapper).mapToUserDTO(mockedUser);
    }

    @Test
    public void addUserReturnsUser() {
        when(mockedUserRepository.findByUsername(username)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.findByEmail(email)).thenReturn(emptyOptionalUser);
        when(mockedUserRepository.save(mockedUser)).thenReturn(mockedUser);
        when(mockedUserMapper.mapToUserDTO(mockedUser)).thenReturn(mockedUserDTO);

        Assertions.assertNotNull(userService.addUser(mockedRegistrationDTO));
    }

    @Test(expected = BadParameterException.class)
    public void updateUserThrowsBadParameterExceptionIfParameterIsNull() {
        userService.updateUser(null);
    }

    @Test
    public void updateUserThrowsBadParameterExceptionWithCorrectMessage() {
        final String expectedMessage = "User is null";

        final String actualMessage = assertThrows(BadParameterException.class, () -> userService.updateUser(null)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void updateUserCallsUserRepositoryFindById() {
        when(mockedUserDTO.getId()).thenReturn(userId);
        when(mockedUserRepository.findById(userId)).thenReturn(optionalUser);

        userService.updateUser(mockedUserDTO);

        verify(mockedUserRepository).findById(userId);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateUserThrowsResourceNotFoundExceptionWhenNoUserIsFoundById() {
        final int userIdThatDoesNotExist = -1;

        when(mockedUserDTO.getId()).thenReturn(userIdThatDoesNotExist);
        when(mockedUserRepository.findById(userIdThatDoesNotExist)).thenReturn(emptyOptionalUser);

        userService.updateUser(mockedUserDTO);
    }

    @Test
    public void updateUserThrowsResourceNotFoundExceptionWithCorrectMessage() {
        final String expectedMessage = "No user found";
        final int userIdThatDoesNotExist = -1;

        when(mockedUserDTO.getId()).thenReturn(userIdThatDoesNotExist);
        when(mockedUserRepository.findById(userIdThatDoesNotExist)).thenReturn(emptyOptionalUser);

        final String actualMessage = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(mockedUserDTO)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void updateUserCallsUserRepositorySaveFunction(){
        when(mockedUserDTO.getId()).thenReturn(userId);
        when(mockedUserRepository.findById(userId)).thenReturn(optionalUser);

        userService.updateUser(mockedUserDTO);

        verify(mockedUserRepository).save(mockedUser);
    }

    @Test
    public void updateUserMapsSavedUserBackToUserDTO(){
        when(mockedUserDTO.getId()).thenReturn(userId);
        when(mockedUserRepository.findById(userId)).thenReturn(optionalUser);
        when(mockedUserRepository.save(mockedUser)).thenReturn(mockedUser);

        userService.updateUser(mockedUserDTO);

        verify(mockedUserMapper).mapToUserDTO(mockedUser);
    }

    @Test
    public void updateUserReturnsUserDTO(){
        when(mockedUserDTO.getId()).thenReturn(userId);
        when(mockedUserRepository.findById(userId)).thenReturn(optionalUser);
        when(mockedUserRepository.save(mockedUser)).thenReturn(mockedUser);
        when(mockedUserMapper.mapToUserDTO(mockedUser)).thenReturn(mockedUserDTO);

        assertNotNull(userService.updateUser(mockedUserDTO));
    }
}
