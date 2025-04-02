package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Branch;

import java.util.List;

public interface BranchService {
    List<Branch> getALlBranch();
    Branch createBranch(Branch branch);
}
