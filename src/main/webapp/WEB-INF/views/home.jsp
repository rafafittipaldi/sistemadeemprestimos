<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Sistema de empr�stimos</title>

<link rel="stylesheet" 	href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css"	integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk"	crossorigin="anonymous">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

</head>
<body>

	<div class="table-responsive-xl">

			<h1 align="center">
				<i class="fa fa-handshake-o"> Sistema de empr�stimos </i> 
			</h1>

		<div class="table-info">
			<nav aria-label="breadcrumb">
			  <ol class="breadcrumb">
			    <li class="breadcrumb-item"><a href='<c:url value="/" />'>Lista de empr�stimos</a></li>
			    <li class="breadcrumb-item"><a href='<c:url value="/novo" />'>Criar novo empr�stimo</a></li>
			  </ol>
			</nav>
		</div>
		
		<table class="table table-sm">
			<thead class="thead-dark">
				<tr>
					<th>N�mero do contrato</th>
					<th>Nome do cliente</th>
					<th>Nome do coletor</th>
					<th>Inicio do contrato</th>
					<th>Fim do contrato</th>
					<th>Saldo montante devido</th>
					<th>Pr�ximo vencimento</th>
					<th>Status</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${emprestimo}" var="emprestimo">
					<tr>
						<td>${emprestimo.numeroDoContrato}</td>
						<td>${emprestimo.cliente.nomeDoCliente}</td>
						<td>${emprestimo.coletor.nomeDoColetor}</td>
						<td>${emprestimo.dataInicioContrato}</td>
						<td>${emprestimo.dataFimContrato}</td>
						<td>${emprestimo.montanteDoEmprestimoDevido}</td>
						<td>${emprestimo.dataProximoVencimento}</td>
						<td>${emprestimo.status}</td>
						<td>
							<a title="Editar" href='<c:url value="/formedit/${emprestimo.numeroDoContrato}" />'><span class="glyphicon glyphicon-pencil"></span></a>
							<a title="Ecluir" href='<c:url value="/delete/${emprestimo.numeroDoContrato}" />'><span class="glyphicon glyphicon-remove"></span></a>
							<span class="glyphicon glyphicon-list"></span>	
							<span class="glyphicon glyphicon-check"></span>						
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	
</body>
</html>