package org.aptech.backendmypham.services.serviceImpl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.repositories.BranchRepository;
import org.aptech.backendmypham.services.BranchService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;

    @Override
    public List<Branch> getALlBranch(){
        return branchRepository.findAll();
    }

    @Override
    public Branch createBranch(Branch branch){
        branchRepository.save(branch);
        return branch;
    }
    @Override
    public Branch findByName(String branchName){
        return branchRepository.findAll()
                .stream()
                .filter(branch -> branch.getName().equals(branchName))
                .findFirst()
                .orElse(null);
    }
    @Override
    public  Branch findByID(Long BiD){
        return branchRepository.findById(BiD).orElse(null);
    }
    @Override
    public void updateBranch(Long branchId, Branch updatedBranch) {
        branchRepository.findById(branchId).ifPresent(branch -> {
            branch.setName(updatedBranch.getName());
            branch.setAddress(updatedBranch.getAddress());
            branch.setIsActive(updatedBranch.getIsActive());
            branchRepository.save(branch);
        });
    }

    @Override
    public void deleteBranch(Long BiD){
        try{
            Optional<Branch> branch = branchRepository.findById(BiD);
            if(branch.isEmpty()){
                throw new RuntimeException("Branch đã bị xóa hoặc không tồn tại!");
            }
            Branch existingBranch = branch.get();
            existingBranch.setIsActive(false);
            branchRepository.save(existingBranch);
        }catch (Exception e){
            throw new RuntimeException("Error occurred while deleting branch: " + e.getMessage());
        }

    }


}
