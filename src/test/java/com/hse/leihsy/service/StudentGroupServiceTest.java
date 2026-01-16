package com.hse.leihsy.service;

import com.hse.leihsy.mapper.StudentGroupMapper;
import com.hse.leihsy.model.dto.CreateStudentGroupDTO;
import com.hse.leihsy.model.dto.StudentGroupDTO;
import com.hse.leihsy.model.dto.UpdateStudentGroupDTO;
import com.hse.leihsy.model.entity.StudentGroup;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.StudentGroupRepository;
import com.hse.leihsy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentGroupService Tests")
class StudentGroupServiceTest {

    @Mock
    private StudentGroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentGroupMapper groupMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private StudentGroupService studentGroupService;

    private User owner;
    private User member;
    private User stranger;
    private StudentGroup testGroup;

    @BeforeEach
    void setUp() {
        owner = new User("owner-id", "Owner User");
        owner.setId(1L);

        member = new User("member-id", "Member User");
        member.setId(2L);

        stranger = new User("stranger-id", "Stranger User");
        stranger.setId(99L);

        testGroup = new StudentGroup();
        testGroup.setId(10L);
        testGroup.setName("Test Group");
        testGroup.setCreatedBy(owner);
        testGroup.setMembers(new HashSet<>(Set.of(owner, member))); // Owner + 1 Member
    }

    @Nested
    @DisplayName("createGroup Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("Sollte Gruppe erstellen und Creator als Member hinzufügen")
        void shouldCreateGroupAndAddCreator() {
            // Arrange
            CreateStudentGroupDTO createDTO = new CreateStudentGroupDTO();
            createDTO.setName("New Group");

            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findByNameIgnoreCase("New Group")).thenReturn(Optional.empty());
            when(groupRepository.save(any(StudentGroup.class))).thenAnswer(inv -> {
                StudentGroup g = inv.getArgument(0);
                g.setId(100L);
                return g;
            });
            when(groupMapper.toDTO(any(StudentGroup.class))).thenReturn(new StudentGroupDTO());

            // Act
            studentGroupService.createGroup(createDTO);

            // Assert
            verify(groupRepository).save(any(StudentGroup.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn Name schon existiert")
        void shouldThrowWhenNameExists() {
            CreateStudentGroupDTO createDTO = new CreateStudentGroupDTO();
            createDTO.setName("Existing Group");

            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findByNameIgnoreCase("Existing Group")).thenReturn(Optional.of(testGroup));

            assertThatThrownBy(() -> studentGroupService.createGroup(createDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("existiert bereits");
        }
    }

    @Nested
    @DisplayName("updateGroup Tests")
    class UpdateGroupTests {

        @Test
        @DisplayName("Sollte Gruppe aktualisieren wenn User Owner ist")
        void shouldUpdateGroupWhenOwner() {
            // Arrange
            UpdateStudentGroupDTO updateDTO = new UpdateStudentGroupDTO();
            updateDTO.setName("Updated Name");

            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(groupRepository.save(any(StudentGroup.class))).thenReturn(testGroup);
            when(groupMapper.toDTO(any())).thenReturn(new StudentGroupDTO());

            // Act
            studentGroupService.updateGroup(10L, updateDTO);

            // Assert
            assertThat(testGroup.getName()).isEqualTo("Updated Name");
            verify(groupRepository).save(testGroup);
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn User nicht Owner ist")
        void shouldThrowWhenNotOwner() {
            UpdateStudentGroupDTO updateDTO = new UpdateStudentGroupDTO();

            when(userService.getCurrentUser()).thenReturn(member); // Member versucht update
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));

            assertThatThrownBy(() -> studentGroupService.updateGroup(10L, updateDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Nur der Ersteller");
        }
    }

    @Nested
    @DisplayName("addMember Tests")
    class AddMemberTests {

        @Test
        @DisplayName("Owner darf neue Mitglieder hinzufügen")
        void shouldAllowOwnerToAddMember() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));
            when(groupRepository.save(any(StudentGroup.class))).thenReturn(testGroup);
            when(groupMapper.toDTO(any())).thenReturn(new StudentGroupDTO());

            // Act
            studentGroupService.addMember(10L, 99L);

            // Assert
            assertThat(testGroup.getMembers()).contains(stranger);
            verify(groupRepository).save(testGroup);
        }

        @Test
        @DisplayName("User darf sich selbst hinzufügen (Join)")
        void shouldAllowSelfJoin() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(stranger); // Stranger will join
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(userRepository.findById(99L)).thenReturn(Optional.of(stranger)); // ID matches current user
            when(groupRepository.save(any(StudentGroup.class))).thenReturn(testGroup);
            when(groupMapper.toDTO(any())).thenReturn(new StudentGroupDTO());

            // Act
            studentGroupService.addMember(10L, 99L);

            // Assert
            assertThat(testGroup.getMembers()).contains(stranger);
        }

        @Test
        @DisplayName("Normales Mitglied darf keine anderen hinzufügen")
        void shouldDenyMemberAddingOthers() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(member); // Member will Stranger adden
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));

            // Act & Assert
            assertThatThrownBy(() -> studentGroupService.addMember(10L, 99L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("nur sich selbst");
        }
    }

    @Nested
    @DisplayName("removeMember Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("Owner darf Mitglieder entfernen")
        void shouldAllowOwnerToRemoveMember() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(userRepository.findById(2L)).thenReturn(Optional.of(member));
            when(groupRepository.save(any(StudentGroup.class))).thenReturn(testGroup);
            when(groupMapper.toDTO(any())).thenReturn(new StudentGroupDTO());

            // Act
            studentGroupService.removeMember(10L, 2L);

            // Assert
            assertThat(testGroup.getMembers()).doesNotContain(member);
            verify(groupRepository).save(testGroup);
        }

        @Test
        @DisplayName("Mitglied darf sich selbst entfernen (Leave)")
        void shouldAllowSelfLeave() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(member);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(userRepository.findById(2L)).thenReturn(Optional.of(member));
            when(groupRepository.save(any(StudentGroup.class))).thenReturn(testGroup);
            when(groupMapper.toDTO(any())).thenReturn(new StudentGroupDTO());

            // Act
            studentGroupService.removeMember(10L, 2L);

            // Assert
            assertThat(testGroup.getMembers()).doesNotContain(member);
        }

        @Test
        @DisplayName("Owner kann nicht entfernt werden")
        void shouldNotAllowRemovingOwner() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(owner); // Owner versucht sich selbst zu löschen
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

            // Act & Assert
            assertThatThrownBy(() -> studentGroupService.removeMember(10L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Ersteller kann nicht");
        }
    }

    @Nested
    @DisplayName("deleteGroup Tests")
    class DeleteGroupTests {

        @Test
        @DisplayName("Sollte Gruppe soft-deleten wenn keine aktiven Bookings")
        void shouldDeleteGroupWhenNoActiveBookings() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(groupRepository.countActiveBookingsByGroupId(10L)).thenReturn(0L); // Keine Bookings

            // Act
            studentGroupService.deleteGroup(10L);

            // Assert
            assertThat(testGroup.getDeletedAt()).isNotNull();
            verify(groupRepository).save(testGroup);
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn aktive Bookings existieren")
        void shouldThrowWhenActiveBookingsExist() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(owner);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));
            when(groupRepository.countActiveBookingsByGroupId(10L)).thenReturn(5L); // 5 aktive Bookings

            // Act & Assert
            assertThatThrownBy(() -> studentGroupService.deleteGroup(10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("aktive Buchungen");

            verify(groupRepository, never()).save(any(StudentGroup.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn nicht Owner")
        void shouldThrowWhenNotOwner() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(member);
            when(groupRepository.findActiveById(10L)).thenReturn(Optional.of(testGroup));

            // Act & Assert
            assertThatThrownBy(() -> studentGroupService.deleteGroup(10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Nur der Ersteller");
        }
    }
}