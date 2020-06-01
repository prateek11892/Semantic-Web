'''
ruleDictionary() defines the Tableau Rules
. is for conjunction(OR)
+ is for disjunction(AND)
~ is for negation(NOT)
* is for impication(=>)
= is for bi implication(<=>)

For rules giving 2 branches - storing in the form [[],[]]
For rules giving only 1 branch - storing in the form [] for negation and [[ , ]] for others

'''
def ruleDictionary():
	dict1={
			'.':{'False':[[False],[False]],'True':[[True,True]]},
    		'+':{'False':[[False,False]],'True':[[True],[True]]},
    		'*':{'False':[[True,False]],'True':[[False],[True]]},
    		'=':{'False':[[True,False],[False,True]],'True':[[True,True],[False,False]]},
			'~':{'False':[True],'True':[False]} 
	}
	return dict1

'''
getRule() fetches the rule from the ruleDictionary() method 
on the basis of operator and the boolean value attached with the expression
'''
def getRule(booleanVal, operator):
	dict1 = ruleDictionary()
	rule = dict1[operator][booleanVal]
	return rule

'''
operatorPos() finds the operator on the basis of the branching needs to be done
Assumption:- If the expression is not an atomic expression then that expression is inside the parenthesis. If atomic, then no need.
Pushing only the opening parenthesis and popping on encountering the closing parenthesis
If only 1 opening parenthesis is on the stack and we encounter an operator, then we separate out the operands and return along with the position of the operator.

'''
def operatorPos(expr):
	operators = ['*','+','.','=']
	neg_operator = ['~']
#	print("In operatorPos")
	operator_stack = []
	operand_stack = []
	index = -1
	for i in expr:
		index += 1
		if i == '(':
			operator_stack.append(i)
		elif i in operators and len(operator_stack) == 1 and operator_stack.pop() == '(':
			ret_list = []
			ret_list.append(expr[1:index])
			ret_list.append(i)
			ret_list.append(expr[index+1:len(expr)-1])
			#return expr[1:index], i, expr[index+1:len(expr)-1]
			return ret_list
			break
		elif i in neg_operator and len(operator_stack) == 1 and operator_stack.pop() == '(':
			ret_list = []
			ret_list.append(i)
			ret_list.append(expr[index+1:len(expr)-1])
			return ret_list
			break

		elif i ==')':
			operator_stack.pop()
	return 0

'''
checkAtomic() - Checks the atomicity of the expression stored in the formula list
If not atomic, then return a boolean value "False" and the position
If atomic, then return "True" and -1
'''
def checkAtomic(formula_list):
	for i in range(len(formula_list)):
		#print("i::::",i)
		#print("formula_list[i]::",len(formula_list[i].split(",")[1]))
		if len(formula_list[i].split(",")[1]) > 1:
			return "False",i
	return "True",-1

'''
breakAtomicFormula() splits the atomic formula of the form True,A or False,A into two values - True/False and A
'''
def breakAtomicFormula(formula):
	formula_split_list = formula.split(",")
	booleanVal = formula_split_list[0]
	variable = formula_split_list[1]
	return booleanVal,variable

'''
checkContradiction() - Checks the contradiction in the list
Stores each expression in the dictionary with variable as key and boolean value as its value.
If already stored in the list, then check for the contradiction.
If contradicting, then invalid and this result will propagate upwards
Else, it will check for next branch.
'''
def checkContradiction(formula_list):
	formula_dict = {}
	for i in range(len(formula_list)):
		booleanVal,variable = breakAtomicFormula(formula_list[i])
		if variable in formula_dict.keys():
			bVal = formula_dict[variable]
			if bVal != booleanVal:
				#print("The path is closed")
				return "Path Closed"
		else:
			formula_dict[variable] = booleanVal
	#print("Path not closed")
	return "Path Not Closed"

'''
removeNestings() - For making a nested list flat
'''
def removeNestings(lst): 
	output = []
	for i in lst:
		if type(i) == list:
			for k in range(len(i)):
				output.append(i[k]) 
		else:
			output.append(i)
	return output

'''
process() - replaces the == to = as the string is broken using character by character
'''
def process(string):
	return string.replace("==","=")

'''
recursiveCall() - recursive function to compute 
'''
def recursiveCall(formula_list):
	isAtomic, i = checkAtomic(formula_list)	# Checks whether the list contains some atomic or not
	#print("isAtomic::",isAtomic)
	#print("i::",i)
	if isAtomic == "False":	# if there is non atomic
		lstVal = formula_list[i]	# take out that formula
		if len(formula_list[i]) > 3:	# if formula length is greater than 3 
			lst = lstVal.split(',')		# split on the basis of comma
			booleanVal = lst[0]			# check the boolean value attached to it
			#print(lst[0])
			#print(lst[1])
			op_list = operatorPos(lst[1])	# retrieve the operator along with the operand(s)
			if len(op_list) == 3:			# if length of operator list is 3 i.e. it is not the case of negation
				opA = op_list[0]
				op = op_list[1]
				opB = op_list[2]
				#print("opA:",opA)
				#print("op:",op)
				#print("opB:",opB)
			elif len(op_list) == 2:			# case of negation
				op = op_list[0]
				opB = op_list[1]

			rule = getRule(booleanVal, op)	# fetch the rule- whether to split in two branches or one branch
			#print("Rule is:",rule)
			if len(rule) == 1:				# if single branch rule is retrieved
				new_list = formula_list[0:i]	# make a new list and append the starting expressions present in the list
				if len(op_list) == 3:			# if the case is of operators other than negation
					new_list.append(str(rule[0][0])+','+opA)
					new_list.append(str(rule[0][1])+','+opB)
				elif len(op_list) == 2:			# if the case is of negation operator
					new_list.append(str(rule[0])+','+opB)
				if len(formula_list[i+1:]) > 0:		# if there are expressions left to be appended
					new_list.append(formula_list[i+1:])
					new_list = removeNestings(new_list)	# make flat list

				#print("new_list:",new_list)
				retVal = recursiveCall(new_list)	# call again the recursive method on the new list
				if retVal == False:
					return False
				else:
					return True
				'''formula_list.remove(formula_list[i])
				formula_list.insert(0,str(rule[0][0])+','+opA)
				formula_list.insert(1,str(rule[0][1])+','+opB)'''
			else:							# if multiple branch rule is retrieved
				lst1 = formula_list[0:i]	# make left list
				lst1.append(str(rule[0][0])+','+opA)
				lst2 = formula_list[0:i]	# make right list
				lst2.append(str(rule[1][0])+','+opB)
				if len(formula_list[i+1:]) > 0:
					lst1.append(formula_list[i+1:])
					lst1 = removeNestings(lst1)
					lst2.append(formula_list[i+1:])
					lst2 = removeNestings(lst2)
				#print("lst1::::",lst1)
				#print("lst2::::",lst2)
				retVal = recursiveCall(lst1)	# call recursive function on these two lists
				if retVal == False:
					return False
				else:
					retVal2 = recursiveCall(lst2)
					if retVal2 == False:
						return False
					else:
						return True
			#print("new_list:",new_list)
		'''else:
			print("to be written")'''

	else:	# if all atomic then check for contradiction in the list
		result = checkContradiction(formula_list)
		if result == "Path Not Closed":
			return False	# return False if the path is not closed
		else:
			return True		# return true if the path is closed
'''
toprove = "False,((~B)*(~A))"
given = "True,(A*B)"'''
'''
toprove = "False,(~((~(P+Q))+P))"
given = "True,(P+Q)"
'''
'''
toprove = "False,((A*B)*(A*C))"
given = "True,(A*(B*C))"
'''
'''toprove = "False,(A.(B+C))"
given = "True,((A.B)+(A.C))"
'''
'''
toprove = "False,P"
given = "True,(P*Q)"
'''
toprove = "False,(P*Q)"
given = "True,((P*(Q+R)).(~R))"

toprove = process(toprove)
given = process(given)

lst = [toprove,given]
print (lst)

endResult = recursiveCall(lst)
if endResult == False:
	print("No")
else:
	print("Yes")
