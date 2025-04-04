package org.aptech.backendmypham.services.serviceImpl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.repositories.BranchRepository;
import org.aptech.backendmypham.services.BranchService;
import org.springframework.stereotype.Service;

import java.util.List;

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



}
