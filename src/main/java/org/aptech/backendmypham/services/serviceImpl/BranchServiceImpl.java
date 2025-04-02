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


}
