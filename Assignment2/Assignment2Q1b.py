from collections import deque

'''
To generate the combinations of the truth table
'''

final_list = []
def makeTheList(arr, n):  
    lst = []
    for i in range(0, n):  
        lst.append(arr[i])
    final_list.append(tuple(lst))
  
def generateAllBooleanStrings(n, arr, i):   
    if i == n: 
        makeTheList(arr, n)  
        return
    arr[i] = True
    generateAllBooleanStrings(n, arr, i + 1)  

    arr[i] = False
    generateAllBooleanStrings(n, arr, i + 1)  
  
def generateLHS(total_variables):
    arr = [None] * total_variables  
    generateAllBooleanStrings(total_variables, arr, 0) 
    return final_list
'''
def generateLHS(total_var_formula):
	table = list(itertools.product([True,False], repeat=total_var_formula))
	return table'''

'''
To evaluate not function
'''
def notOperator(p):
	return eval('not p')

'''
To evaluate or with operands
'''
def orOperator(p,q):
	return eval('p or q')

'''
To evaluate and with operands
'''
def andOperator(p,q):
	return eval('p and q')

'''
To evaluate implication with operands
'''
def implication(p,q):
	return eval('(not p) or q')

'''
To evaluate bi-implication with operands
'''
def biimplication(p,q):
	return eval('(p and q) or ((not p) and (not q))')

'''
Find unique variables in the set
'''
def countUniqueVariables(expr_List):
	unique_variables_set = set()
	for i in range(len(expr_List)):
		if expr_List[i].isalpha():
			unique_variables_set.add(expr_List[i])
	return len(unique_variables_set), unique_variables_set


# + is for disjunction or or
# . is for conjunction or and
# ~ is for negation or not
# * is for implication
# == is for bi implication

#expr = "( not p or ( not q or ( r and s ) and ( r or s ) ) )"
#expr = "( ( p . q ) + ( ( ~ p ) . ( ~ q ) ) )"
#expr = "( p * q )"
#expr = "( p == q )"
#expr = "( ( p . q ) * ( q . r ) )"
expr = "( ( p . ( q . r ) ) == ( ( p . q ) . r ) )"
#expr = "( ( p * q ) . ( q * r ) )"
expr_List = expr.split(" ")

total_variables, unique_variables_set = countUniqueVariables(expr_List)
truthtable_List = generateLHS(total_variables)	# generate left hand side of truth table on the basis of total number of variables
result_list = []
dnf_list = []
for row in truthtable_List:
	intermediate_list = []
	operator_stack = deque()
	operand_stack = deque()
	value_dict = {}				
	sorted_unique_variables = sorted(unique_variables_set)
	i = 0
	for i in range(len(sorted_unique_variables)):
		globals()[list(sorted_unique_variables)[i]] = row[i]
	
	for i in range(len(expr_List)):
		#print("operator_stack:",operator_stack)
		#print("operand_stack:",operand_stack)
		if expr_List[i] == '~' or expr_List[i] == '+' or expr_List[i] == '.':
			operator_stack.append(expr_List[i])
		elif expr_List[i].isalpha():
			operand_stack.append(expr_List[i])
		elif expr_List[i] == ')':
			operator = operator_stack.pop()
			if operator == '~':
				a = eval(operand_stack.pop())
				operand_stack.append(str(notOperator(a)))
			elif operator == '.':
				b = eval(operand_stack.pop())
				a = eval(operand_stack.pop())
				operand_stack.append(str(andOperator(a,b)))
			elif operator == '+':
				b = eval(operand_stack.pop())
				a = eval(operand_stack.pop())
				operand_stack.append(str(orOperator(a,b)))
			elif operator == '*':
				b = eval(operand_stack.pop())
				a = eval(operand_stack.pop())
				operand_stack.append(str(implication(a,b)))
			elif operator == '==':
				b = eval(operand_stack.pop())
				a = eval(operand_stack.pop())
				operand_stack.append(str(biimplication(a,b)))
			operator_stack.pop()
		else:
			operator_stack.append(expr_List[i])

	intermediate_list.append(row)
	intermediate_list.append(operand_stack[0])
	result_list.append(intermediate_list)			
	if operand_stack[0] == 'True':				#	to make dnf, we need to pick the variables for the result is True
		dnf_list.append('(')
		for i in range(len(sorted_unique_variables)):
			if row[i] == True:					#	add a true value of the variable if it is true
				dnf_list.append(list(sorted_unique_variables)[i])
				dnf_list.append('and')
			elif row[i] == False:				# add the complemented value of the variable if it is false
				dnf_list.append('not')
				dnf_list.append(list(sorted_unique_variables)[i])
				dnf_list.append('and')
		dnf_list.pop()
		dnf_list.append(')')
		dnf_list.append('or')
dnf_list.pop()					#	remove the last or
print(result_list)				#	print the truth table
print("dnf_list::",' '.join(dnf_list))	