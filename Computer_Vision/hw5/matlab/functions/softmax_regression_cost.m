function [cost,grad] = softmax_regression_cost(weight, X, y)
  %
  % Arguments:
  %   weight - A vector containing the parameter values to optimize.
  %       In minFunc, weight is reshaped to a long vector.  So we need to
  %       resize it to an n-by-(num_classes-1) matrix.
  %       Recall that we assume weight(:,num_classes) = 0.
  %
  %   X - The examples stored in a matrix.  
  %       X(i,j) is the i'th coordinate of the j'th example.
  %   y - The label for each example.  y(j) is the j'th example's label.
  %
  
  m=size(X,2);
  n=size(X,1);

  % weight is a vector;  need to reshape to n x num_classes.
  weight=reshape(weight, n, []);
  num_classes=size(weight,2)+1;
  
  % initialize objective value and gradient.
  cost = 0;
  grad = zeros(size(weight));
 
  %
  % TODO:  Compute the softmax objective function and gradient using vectorized code.
  %        Store the objective function value in 'cost', and the gradient in 'grad'.
  %        Before returning grad, make sure you form it back into a vector with grad=grad(:);
  %
  %% === YOUR CODE HERE === %%
 
    
   %% ===================== %%
    grad=grad(:); % make gradient a vector for minFunc
